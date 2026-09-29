# Android Git SSH

自用 Android Git 客户端：Kotlin + Jetpack Compose + JGit，SSH 推送到 GitHub，支持 force push，内置 shell。

只编 **arm64-v8a**。

## 功能

- Git 身份 / GitHub 用户 / `git@github.com:user/repo.git`
- 生成或导入 Ed25519/RSA SSH 密钥，复制公钥
- 选择代码根目录（SAF / 应用目录 / Downloads）
- `init`、`clone`、提交、增量 push、`pull --rebase`、**force push**
- 终端：`git` 走 JGit；其它命令走 `/system/bin/sh`

## 本机编译

需要 JDK 17 + Android SDK。

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/
```

CI：`.github/workflows/build.yml` 在 push 到 `main` 时编 debug APK 并上传 artifact。

## 使用

1. 打开「所有文件访问」（私用，方便 JGit 直接写真实路径）
2. 配置页填 name/email/GitHub 用户/远程 SSH 地址
3. 生成密钥，把公钥贴到 GitHub SSH keys
4. 选仓库目录，init 或 clone
5. 推送页提交后 push / force push
6. 终端可执行 `git status`、`ls`、`cd` 等

## 注意

- 系统没有原生 `git` 二进制，终端里的 git 子命令由 JGit 实现。
- force push 会覆盖远程分支，仅用于你自己的小仓库。
- 私钥存在 EncryptedSharedPreferences + 应用私有 `files/ssh/`。
