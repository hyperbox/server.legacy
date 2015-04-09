/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
 * 
 * http://kamax.io/hbox/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.hboxd.task;

import net.engio.mbassy.listener.Handler;
import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.exception.HyperboxCommunicationException;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.states.ServerState;
import io.kamax.hbox.states.TaskQueueEvents;
import io.kamax.hbox.states.TaskState;
import io.kamax.hboxd.comm.io.factory.TaskIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action._HyperboxAction;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event.system.SystemStateEvent;
import io.kamax.hboxd.event.task.TaskQueueEvent;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.hboxd.task._Task;
import io.kamax.hboxd.task._TaskManager;
import io.kamax.tool.logging.LogLevel;
import io.kamax.tool.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public final class TaskManager implements _TaskManager {

   private volatile boolean running;
   private long taskId = 1;
   private volatile _Task currentTask;

   private Map<String, _Task> tasks;
   private BlockingQueue<_Task> taskQueue;
   private BlockingDeque<_Task> finishedTaskDeque;

   private TaskQueueWorker queueWorker = new TaskQueueWorker();
   private Thread worker;

   private _Hyperbox hbox;

   @Override
   public void start(_Hyperbox hbox) {

      this.hbox = hbox;

      taskQueue = new LinkedBlockingQueue<_Task>();
      finishedTaskDeque = new LinkedBlockingDeque<_Task>(10);
      Logger.debug("Task history size: " + finishedTaskDeque.remainingCapacity());
      tasks = new HashMap<String, _Task>();
      worker = new Thread(queueWorker, "TaskMgrQW");
      SecurityContext.addAdminThread(worker);

      EventManager.register(this);
   }

   @Override
   public void stop() {

      running = false;
      if ((worker != null) && !Thread.currentThread().equals(worker)) {
         worker.interrupt();
         try {
            worker.join(1000);
         } catch (InterruptedException e) {
            Logger.debug("Error while waiting for Task Manager worker thread to finish : " + e.getMessage());
            Logger.exception(e);
         }
      }
   }

   @Override
   public void process(Request req) {

      try {
         Logger.debug("Received Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "] "
               + "from Client #" + SessionContext.getClient().getId() + " ("
               + SessionContext.getClient().getAddress() + ")"
               + " under " + SecurityContext.getUser().getName());
         hbox.getSecurityManager().authorize(req);
         _HyperboxAction ac = hbox.getActionManager().get(req);
         if (ac.isQueueable() && req.isQueueable()) {
            queueWorker.queue(req, ac);
         } else {
            Logger.debug("Immediate execute of Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]");
            TaskWorker.execute(req, ac, hbox);
         }
      } catch (HyperboxCommunicationException e) {
         Logger.debug("Communication error : " + e.getMessage());
         SessionContext.getClient().putAnswer(new Answer(req, AnswerType.UNKNOWN, e));
      }
   }

   @Override
   public List<_Task> list() {
      List<_Task> taskList = new ArrayList<_Task>();
      if ((currentTask != null) && !taskQueue.contains(currentTask)) {
         taskList.add(currentTask);
      }
      for (_Task task : taskQueue) {
         taskList.add(task);
      }
      for (_Task task : finishedTaskDeque) {
         taskList.add(task);
      }
      return taskList;
   }

   @Override
   public void remove(String taskId) {

      _Task task = get(taskId);
      task.cancel();
      queueWorker.remove(task);
   }

   @Override
   public _Task get(String taskId) {
      if (!tasks.containsKey(taskId)) {
         throw new HyperboxException("Unknown Task ID: " + taskId);
      }

      return tasks.get(taskId);
   }

   private static class TaskWorker {

      public static void execute(Request req, _HyperboxAction ca, _Hyperbox hbox) {
         try {
            Logger.debug("Processing Request #" + req.getExchangeId());

            SessionContext.getClient().putAnswer(new Answer(req, ca.getStartReturn()));
            try {
               ca.run(req, hbox);
               Logger.debug("Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + " succeeded.");
               SessionContext.getClient().putAnswer(new Answer(req, ca.getFinishReturn()));
            } catch (HyperboxException e) {
               Logger.debug("Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + " failed: " + e.getMessage());
               if (Logger.isLevel(LogLevel.Debug)) {
                  Logger.exception(e);
               }
               SessionContext.getClient().putAnswer(new Answer(req, ca.getFailReturn(), e));
            }
         } catch (Throwable e) {
            Logger.debug("Server Error when executing #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + ": "
                  + e.getMessage());
            Logger.exception(e);
            SessionContext.getClient().putAnswer(new Answer(req, AnswerType.SERVER_ERROR, e));
         }
      }

   }

   private class TaskQueueWorker implements Runnable {

      private boolean add(_Task t) {

         if (!taskQueue.offer(t)) {
            return false;
         }
         tasks.put(t.getId(), t);
         t.queue();
         Logger.debug("Added Request #" + t.getRequest().getExchangeId() + " [" + t.getRequest().getCommand() + ":" + t.getRequest().getName() + "] to queue.");
         EventManager.post(new TaskQueueEvent(TaskQueueEvents.TaskAdded, t));
         return true;
      }

      // TODO use events to clean up the queues
      private void remove(_Task t) {

         if (t != null) {
            taskQueue.remove(t);
            if (!finishedTaskDeque.contains(t) && (finishedTaskDeque.remainingCapacity() == 0)) {
               _Task oldTask = finishedTaskDeque.pollLast();
               tasks.remove(oldTask.getId());
               Logger.debug("Removed Request #" + oldTask.getRequest().getExchangeId() + " [" + oldTask.getRequest().getCommand() + ":"
                     + oldTask.getRequest().getName() + "] from queue.");
               EventManager.post(new TaskQueueEvent(TaskQueueEvents.TaskRemoved, oldTask));
            }
            finishedTaskDeque.offerFirst(t);
            Logger.debug("Archived Request #" + t.getRequest().getExchangeId() + " [" + t.getRequest().getCommand() + ":" + t.getRequest().getName()
                  + "]");
         }
      }

      public void queue(Request req, _HyperboxAction ca) {

         Logger.debug("Queueing Request #" + req.getExchangeId());
         SessionContext.getClient().putAnswer(new Answer(req, AnswerType.STARTED));
         // TODO do a better Task ID generator
         _Task t = new HyperboxTask(Long.toString(taskId++), ca, req, SecurityContext.getUser(), SessionContext.getClient(), hbox);
         if (add(t)) {
            SessionContext.getClient().putAnswer(new Answer(req, AnswerType.DATA, TaskIoFactory.get(t)));
            SessionContext.getClient().putAnswer(new Answer(req, AnswerType.QUEUED));
         } else {
            Logger.debug("Failed to queue Request - Queue is full");
            SessionContext.getClient().putAnswer(new Answer(req, AnswerType.SERVER_ERROR));
         }
      }

      @Override
      public void run() {

         running = true;
         Logger.verbose("Task Queue Worker started");

         while (running) {
            try {
               while ((taskQueue.peek() == null) || taskQueue.peek().getState().equals(TaskState.Created)) {
                  Thread.sleep(100);
               }
               currentTask = taskQueue.take();
               currentTask.start();
            } catch (InterruptedException e) {
               Logger.debug("Task Queue Worker was interupted, halting...");
               running = false;
            } catch (Throwable e) {
               Logger.error("Exception in Task #" + currentTask.getId() + " : " + e.getMessage());
            } finally {
               remove(currentTask);
               currentTask = null;
            }
         }
         Logger.info("Task Queue Worker halted");
      }
   }

   @Handler
   public void putSystemEvent(SystemStateEvent ev) {

      if (ServerState.Running.equals(ev.getState())) {
         worker.start();
      }
   }

   @Override
   public long getHistorySize() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void setHistorySize(long size) {
      // TODO Auto-generated method stub

   }

}
