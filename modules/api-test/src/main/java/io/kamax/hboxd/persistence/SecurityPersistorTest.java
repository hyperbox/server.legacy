/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
 * hyperbox at altherian dot org
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

package io.kamax.hboxd.persistence;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.tools.logging.LogLevel;
import io.kamax.tools.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import static org.junit.Assert.assertTrue;

public abstract class SecurityPersistorTest {

    @Rule
    public TestName testName = new TestName();

    private static boolean initialized = false;
    protected static _SecurityPersistor persistor;

    public static void init(_SecurityPersistor persistor) throws HyperboxException {
        Logger.setLevel(LogLevel.Tracking);

        SecurityPersistorTest.persistor = persistor;
        initialized = true;
    }

    @Before
    public void before() {
        assertTrue("SecurityPersistorTest is not initiazlied, call init() in @BeforeClass", initialized);
        System.out.println("--------------- START OF " + testName.getMethodName() + "-----------------------");
    }

    @After
    public void after() {
        System.out.println("--------------- END OF " + testName.getMethodName() + "-----------------------");
    }

}
