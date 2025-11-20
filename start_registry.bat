@echo off
setlocal

REM Try to get IP using Python (most reliable)
for /f "delims=" %%i in ('python -c "import socket; s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM); s.connect(('8.8.8.8', 80)); print(s.getsockname()[0]); s.close()" 2^>nul') do set HOST_IP=%%i

REM Fallback: Manual IP entry if Python fails
if "%HOST_IP%"=="" (
    echo Could not automatically detect IP using Python.
    echo Please enter your local IP address (check ipconfig):
    set /p HOST_IP=
)

if "%HOST_IP%"=="" set HOST_IP=127.0.0.1

echo Detected IP: %HOST_IP%
echo Starting RMI Registry...
echo.

if not exist RegistryServer.class (
    echo Compiling...
    javac RegistryServer.java
)

java -Djava.rmi.server.hostname=%HOST_IP% -Dregistry.port=1099 RegistryServer
