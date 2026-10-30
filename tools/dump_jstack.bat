@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem 抓取 Minecraft 主线程的现场。
rem 用法：游戏卡住时（不要关游戏）双击本脚本，它会对每个 java 进程导出线程转储。
rem 结果：jstack-<pid>-<时间>.txt

set OUT=%~dp0
set FOUND=0

for /f "tokens=1,2" %%a in ('jps -l 2^>nul') do (
    echo %%b | findstr /i "Main" >nul
    if !errorlevel! equ 0 (
        set FOUND=1
        echo [抓取] pid=%%a
        jstack -l %%a > "%OUT%jstack-%%a.txt" 2>nul
        if exist "%OUT%jstack-%%a.txt" (
            echo        已保存 jstack-%%a.txt
        ) else (
            echo        失败，可能 jstack 不在 PATH，尝试 JAVA_HOME
            if defined JAVA_HOME (
                "%JAVA_HOME%\bin\jstack.exe" -l %%a > "%OUT%jstack-%%a.txt" 2>nul
                if exist "%OUT%jstack-%%a.txt" echo        已通过 JAVA_HOME 保存
            )
        )
    )
)

if !FOUND! equ 0 (
    echo [错误] 没找到 Minecraft 进程。
    echo 请确认游戏还开着，或先运行 jps -l 看看有哪些 java 进程。
)

echo.
echo 完成。把 jstack-*.txt 发给开发即可。
pause
