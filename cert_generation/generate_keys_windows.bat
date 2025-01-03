@echo off
REM ##########################################################
REM # Script: run_generate_keys.bat
REM # Zweck:  - Prüft WSL-Installation
REM #         - Installiert Ubuntu (falls nicht vorhanden)
REM #         - Kopiert generate_keys.sh in die WSL-Umgebung
REM #         - Führt das Skript aus
REM #         - Kopiert Zertifikate zurück nach Windows
REM ##########################################################

echo.
echo =========================================================
echo 1. Prüfen, ob WSL installiert ist
echo =========================================================
where wsl
IF %ERRORLEVEL% NEQ 0 (
    echo WSL scheint nicht installiert zu sein.
    echo Versuche nun, WSL zu installieren ...
    echo (Dies erfordert Administratorrechte und einen Neustart.)
    wsl --install
    echo Bitte starte deinen Computer neu und fuehre dieses Skript dann erneut aus.
    pause
    exit /b 0
)
ELSE (
    echo WSL ist installiert.
)

echo.
echo =========================================================
echo 2. Prüfen, ob Ubuntu in WSL vorhanden ist
echo =========================================================
REM Wir schauen, ob "Ubuntu" in der Liste aufscheint
wsl -l -q | findstr /I "Ubuntu" >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo Ubuntu ist nicht installiert.
    echo Versuche nun, Ubuntu zu installieren ...
    echo (Dies erfordert Administratorrechte und einen Neustart.)
    wsl --install -d Ubuntu
    echo Bitte starte deinen Computer neu und fuehre dieses Skript erneut aus.
    pause
    exit /b 0
)

echo.
echo =========================================================
echo 3. Skript "generate_keys.sh" ueberpruefen
echo =========================================================
IF NOT EXIST "generate_keys.sh" (
    echo Fehler: "generate_keys.sh" wurde im aktuellen Ordner nicht gefunden.
    pause
    exit /b 1
)

echo.
echo =========================================================
echo 4. Skript in ein temporäres Verzeichnis kopieren
echo =========================================================
REM Wir kopieren das Skript erst ins TEMP-Verzeichnis, um es von dort aus
REM in die Linux-Umgebung zu bringen. Du kannst auch ein anderes Verzeichnis nutzen.
set TMP_DIR=%TEMP%\wsl_genkeys
if not exist "%TMP_DIR%" mkdir "%TMP_DIR%"

copy "generate_keys.sh" "%TMP_DIR%\generate_keys.sh" >nul

echo.
echo =========================================================
echo 5. Skript in die Ubuntu-Umgebung kopieren und ausfuehren
echo =========================================================
REM Wir legen in der Ubuntu-Heimumgebung einen Ordner an und kopieren das Skript hinein
wsl bash -c "mkdir -p ~/genkeys && rm -rf ~/genkeys/*"

REM Nun die Datei(n) aus Windows ins Ubuntu-Home kopieren:
REM /mnt/c/... entspricht dem Laufwerk C:\ unter WSL.
REM Beachte: %TMP% enthaelt Backslashes, wir muessen in den WSL-Pfad /mnt/c/ konvertieren.
REM Ein Beispiel: C:\Users\<Name>\AppData\Local\Temp -> /mnt/c/Users/<Name>/AppData/Local/Temp
set TMP_DIR_UNIX=/mnt/c%TMP_DIR:\=/%

wsl bash -c "cp %TMP_DIR_UNIX%/generate_keys.sh ~/genkeys/generate_keys.sh && chmod +x ~/genkeys/generate_keys.sh"

REM Jetzt wird das Skript ausgefuehrt
wsl bash -c "cd ~/genkeys && ./generate_keys.sh"

echo.
echo =========================================================
echo 6. Zertifikate zurueck nach Windows kopieren
echo =========================================================
REM Nun holen wir das gesamte certificates-Verzeichnis
REM und legen es z.B. auf den Desktop des aktuellen Users.
REM Du kannst das nach Belieben anpassen.

set DESKTOP_PATH=C:\Users\%USERNAME%\Desktop
if not exist "%DESKTOP_PATH%\certificates" mkdir "%DESKTOP_PATH%\certificates"

wsl bash -c "cp -r ~/genkeys/certificates /mnt/c/Users/%USERNAME%/Desktop/"

echo.
echo =========================================================
echo Vorgang abgeschlossen.
echo Die Zertifikate findest du nun im Verzeichnis:
echo   %DESKTOP_PATH%\certificates
echo =========================================================
pause
exit /b 0
