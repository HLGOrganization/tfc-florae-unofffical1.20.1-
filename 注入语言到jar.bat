@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 把 src\main\resources\assets\tfcflorae\lang\zh_cn.json 写入已部署的模组 jar
echo.
"C:\Users\hsy66\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe" tools\inject_lang.py
if errorlevel 1 (
  echo.
  echo [失败] 请把上面的报错发给助手
) else (
  echo.
  echo [完成] 重启游戏后生效
)
pause
