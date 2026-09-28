@echo off
set CMD_DIR=%~dp0
if exist "%CMD_DIR%tools\maven\bin\mvn.cmd" (
    "%CMD_DIR%tools\maven\bin\mvn.cmd" %*
) else (
    mvn %*
)
