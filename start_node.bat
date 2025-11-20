@echo off
setlocal

if "%1"=="" (
    echo Usage: %0 ^<registry_ip^> [node_id]
    echo Example: %0 192.168.1.100
    echo Example: %0 192.168.1.100 1
    exit /b 1
)

set REGISTRY_IP=%1
set NODE_ID=%2
if "%NODE_ID%"=="" set NODE_ID=0

REM Try to get IP using Python
for /f "delims=" %%i in ('python -c "import socket; s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM); s.connect(('8.8.8.8', 80)); print(s.getsockname()[0]); s.close()" 2^>nul') do set HOST_IP=%%i

REM Fallback: Manual IP entry if Python fails
if "%HOST_IP%"=="" (
    echo Could not automatically detect IP using Python.
    echo Please enter your local IP address (check ipconfig):
    set /p HOST_IP=
)

if "%HOST_IP%"=="" set HOST_IP=127.0.0.1

echo Starting Node on %HOST_IP%
echo Connecting to registry at: %REGISTRY_IP%:1099
echo Node ID: %NODE_ID%
echo.

if not exist RicartAgrawalaApp.class (
    echo Compiling...
    javac *.java
)

echo Starting Ricart-Agrawala Node in multi-machine mode...
echo.

java -Djava.rmi.server.hostname=%HOST_IP% -Dregistry.host=%REGISTRY_IP% -Dregistry.port=1099 RicartAgrawalaApp multi %NODE_ID%
