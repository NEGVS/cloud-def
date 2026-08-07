
# 0-Open Docker todo
# Mac终端打开Docker Desktop（图形客户端）
```bash
# 最简命令，直接拉起Docker Desktop图形程序
open -a Docker
```

等价完整路径写法：
```bash
open /Applications/Docker.app
```

后台静默启动，不弹出主窗口（只跑后台引擎，右上角出现鲸鱼图标）
```bash
open -a Docker --background
```

---
## 常用配套命令
```bash
# 查看docker引擎是否就绪，引擎启动需要几秒
docker info

# 关闭Docker Desktop
osascript -e 'quit app "Docker"'
```

> ⚠️注意：`open -a Docker`只是打开Mac上的Docker Desktop图形应用；**不是直接启动docker引擎**，执行完后要等几秒，右上角鲸鱼图标停止跳动，`docker ps`才可以正常使用。

### 一键脚本（zsh，可以加到 ~/.zshrc 别名）
```bash
alias docker-up='open -a Docker; echo "等待Docker启动…"; sleep 4; docker info'
```
终端直接敲 `docker‑up`，一键打开并等待就绪。

## 常见报错
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock
```
代表Docker Desktop还没初始化完成，多等几秒即可。

## 打开Docker Desktop内置仪表盘（浏览器弹窗）
```bash
open docker-desktop://dashboard/open
```
