/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013-2015 Maxime Dor
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

!define VERSION "@SERVER_VERSION@"
Name "Hyperbox Server"
OutFile "@SERVER_INSTALLER_OUTPUT@"
InstallDir "@SERVER_INSTALL_DIR@"
RequestExecutionLevel admin

Page directory
Page components
Page instfiles

Section "Core files"
SetOutPath $INSTDIR

# We try to stop & delete the existing service if it exists, else the install will fail
IfFileExists $INSTDIR\hboxd.exe 0 +2
ExecWait '$INSTDIR\hboxd.exe delete hboxd'
# Legacy location of hboxd.exe
IfFileExists $INSTDIR\bin\hboxd.exe 0 +2
ExecWait '$INSTDIR\bin\hboxd.exe delete hboxd'

# Removing libraries and binaries
RMDir /r "$INSTDIR\bin"
RMDir /r "$INSTDIR\lib"

File /r "@SERVER_OUT_BIN_DIR@\bin"
File /r "@SERVER_OUT_BIN_DIR@\doc"
File /r "@SERVER_OUT_BIN_DIR@\lib"
File /r "@SERVER_OUT_BIN_DIR@\modules"
File "@SERVER_OUT_BIN_DIR@\hboxd.exe"
File "@SERVER_OUT_BIN_DIR@\hyperbox.exe"
WriteUninstaller $INSTDIR\uninstaller.exe
SectionEnd

Section "Start Menu Shortcuts"
SetShellVarContext all
CreateDirectory "$STARTMENU\Programs\Hyperbox\Server"
CreateShortCut "$STARTMENU\Programs\Hyperbox\Server\Uninstall Hyperbox Server.lnk" "$INSTDIR\uninstaller.exe"
SectionEnd

Section "Install Service"
ExecWait '$INSTDIR\hboxd.exe install hboxd --DisplayName="Hyperbox" --StartMode=jvm --StopMode=jvm --StartClass="io.kamax.hboxd.HyperboxService" --StartMethod=start --StopClass="io.kamax.hboxd.HyperboxService" --StopMethod=stop --Startup=auto --Classpath="$INSTDIR\bin\*;$INSTDIR\lib\*"'
SectionEnd

Section "Start Service"
ExecWait '$INSTDIR\hboxd.exe start hboxd'
SectionEnd

Section "Uninstall"
SetShellVarContext all

ExecWait '$INSTDIR\hboxd.exe stop hboxd'
ExecWait '$INSTDIR\hboxd.exe delete hboxd'

RMDir /r "$INSTDIR\bin"
RMDir /r "$INSTDIR\doc"
RMDir /r "$INSTDIR\lib"
RMDir /r "$INSTDIR\modules"
Delete "$INSTDIR\hboxd.exe"
Delete "$INSTDIR\hyperbox.exe"
Delete "$INSTDIR\uninstaller.exe"

RMDir /r "$STARTMENU\Programs\Hyperbox\Server"
RMDir "$STARTMENU\Programs\Hyperbox"
SectionEnd
