- [前置工作](#前置工作)
- [前端极速调试（Docker 全量环境）](#前端极速调试docker-全量环境)
- [启动后端服务](#启动后端服务)
  - [本地 IDEA](#本地-idea)
    - [配置环境变量](#配置环境变量)
    - [配置 IDEA 参数](#配置-idea-参数)
- [启动前端服务](#启动前端服务)
  - [连接预发或正式环境](#连接预发或正式环境)
- [其他配置](#其他配置)
  - [配置第三方登录以GitHub为例](#配置第三方登录以github为例)
  - [配置Resend邮箱服务](#配置resend邮箱服务)
- [API文档](#api文档)
  - [OpenAPI文档](#openapi文档)
  - [部署时间线以及文档时效性](#部署时间线以及文档时效性)
  - [OpenAPI文档使用](#openapi文档使用)
  - [OpenAPI文档应用场景](#openapi文档应用场景)

## 前置工作

先克隆仓库：

```shell
git clone https://github.com/nagisa77/OpenIsle.git
cd OpenIsle
```

- 后端开发环境
  - JDK 17+
- 前端开发环境
  - Node.JS 20+

## 前端极速调试（Docker 全量环境）

想要最快速地同时体验前端和后端，可直接使用仓库提供的 Docker Compose。该方案会一次性拉起数据库、消息队列、搜索、后端、WebSocket 以及前端 Dev Server，适合需要全链路联调的场景。

1. 准备环境变量文件：

   ```shell
   cp .env.example .env
   ```

   `.env.example` 是模板，可在 `.env` 中按需覆盖如端口、密钥等配置。确保 `NUXT_PUBLIC_API_BASE_URL`、`NUXT_PUBLIC_WEBSOCKET_URL` 等仍指向 `localhost`，方便前端直接访问容器映射端口。

2. 启动 Dev Profile：

   ```shell
   docker compose \
     -f docker/docker-compose.yaml \
     --env-file .env \
     --profile dev up -d
   ```

   该命令会创建名为 `frontend_dev` 的容器并运行 `npm run dev`，浏览器访问 http://127.0.0.1:3000 即可查看页面。

3. 查看服务状态：

   ```shell
   docker compose -f docker/docker-compose.yaml --env-file .env ps
   docker compose -f docker/docker-compose.yaml --env-file .env logs -f frontend_dev
   ```

4. 停止所有容器：

   ```shell
   docker compose -f docker/docker-compose.yaml --env-file .env --profile dev down
   ```

如需自定义 Node 依赖缓存、数据库持久化等，可参考 `docker/docker-compose.yaml` 中各卷的定义进行调整。

## 启动后端服务

启动后端服务有多种方式，选择一种即可。

> [!IMPORTANT]
> 仅想修改前端的朋友可不用部署后端服务。转到 [启动前端服务](#启动前端服务) 章节。

### 本地 IDEA

```shell
cd backend/
```

IDEA 打开 `backend/` 文件夹。

#### 配置环境变量

1. 在 IDEA 中配置「Environment file」：将 `Run/Debug Configuration` 的 `Environment variables` 指向刚刚复制的 `.env`，即可让 IDE 读取该文件。
2. 需要调整端口或功能开关时，优先修改 `.env`，例如：
   ```ini
   SERVER_PORT=8081
   LOG_LEVEL=DEBUG
   ```

也可以修改 `src/main/resources/application.properties`，但该文件会被 Git 追踪，通常不推荐。

![backend_img_5.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/89658e5d5c0443a5939ef57ccfeab740.png)

#### 配置 IDEA 参数

- 设置 JDK 版本为 Java 17。
- 设置 VM Option，最好运行在其他端口（例如 `8081`）。若已经在 `open-isle.env` 中调整端口，可省略此步骤。
  ```shell
  -Dserver.port=8081
  ```

![backend_img_3.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/6905f7cd4b694b1fa7214dd2b3ef9d81.png)

![backend_img_2.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/2bf8e139c21f4b529384ed68cef053da.png)

完成环境变量和运行参数设置后，即可启动 Spring Boot 应用。

![backend_img_4.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/474d995ddda34b6f80badffa58cec5b9.png)

## 启动前端服务

> [!IMPORTANT]
> **⚠️ 环境要求：Node.js 版本最低 20.0.0（因为 Nuxt 框架要求）**

```shell
cd frontend_nuxt/
```

安装依赖并启动开发服务器：

```shell
npm install --verbose
npm run dev
```

默认情况下，浏览器访问 http://127.0.0.1:3000 即可访问前端页面。

### 连接预发或正式环境

前端默认读取 `.env` 中的接口地址，可通过修改以下变量快速切换到预发或正式环境：

1. 复制对应模板：

   ```shell
   # 使用预发配置
   cp .env.staging.example .env

   # 或者使用正式环境配置
   cp .env.production.example .env
   ```

2. 按需覆盖关键变量：

   ```ini
   NUXT_PUBLIC_API_BASE_URL=https://www.staging.open-isle.com
   NUXT_PUBLIC_WEBSOCKET_URL=https://www.staging.open-isle.com
   ```

   将 `staging` 替换为 `www` 即可连接正式环境。其他变量（如 OAuth Client ID、站点地址等）可根据需求调整。

3. 已经存在 `.env` 时，可直接编辑上述变量并重启 `npm run dev` 让配置生效。

## 其他配置

### 配置第三方登录以GitHub为例

- 修改 `application.properties` 配置

  ![backend_img.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/1dbb388cd1004e1d8822224cf87c9303.png)

- 修改 `.env` 配置

  ![fontend_img.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/3cc276e4795a407a90a47f2d77de2760.png)

- 配置第三方登录回调地址

  ![github_img.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/14667457a15c4fbea9d91226797b7b59.png)

  ![github_img_2.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/7e901e1c648f4330be1d248379d699f1.png)

### 配置Resend邮箱服务

https://resend.com/emails 创建账号并登录

- `Domains` -> `Add Domain`
  ![image-20250906150459400.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/0168a039ca5e47239ab859f6049c4c93.png)
- 填写域名
  ![image-20250906150541817.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/d328cd89f21c4b8fb50c8213c9b784f0.png)
- 等待一段时间后解析成功，创建 key
  `API Keys` -> `Create API Key`，输入名称，设置 `Permission` 为 `Sending access`
  **Key 只能查看一次，务必保存下来**
  ![image-20250906150811572.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/d72959670b22451cb93e94d6e8316e05.png)
  ![image-20250906150924975.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/b46f1a56f2d744a381d361ac8369a231.png)
  ![image-20250906150944130.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/8eaba3fa791f4b10a5c24948812d306d.png)
- 修改 `.env` 配置中的 `RESEND_API_KEY` 和 `RESEND_FROM_EMAIL`
  `RESEND_FROM_EMAIL`： **noreply@域名**
  `RESEND_API_KEY`：**刚刚复制的 Key**
  ![image-20250906151218330.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/8319dc5037bb40b68ed35358d810552a.png)

## API文档

### OpenAPI文档

https://docs.open-isle.com

### 部署时间线以及文档时效性

我已经将API Docs的部署融合进本站CI & CD中，目前如下

- 每次合入main之后，都会构建预发环境 http://staging.open-isle.com/ ,现在文档是紧随其后进行部署，也就是说代码合入main之后，如果是新增后台接口，就可以立即通过OpenAPI文档页面进行查看和调试，但是如果想通过OpenAPI调试需要选择预发环境的
- 每日凌晨三点会构建并重新部署正式环境，届时当日合入main的新后台API也可以通过OpenAPI文档页面调试

![CleanShot 2025-09-10 at 12 .04.48@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/168303009f4047ca828344957e911ff1.png)

👆如图是合入main之后构建预发+docs的情形，总大约耗时4分钟左右

### OpenAPI文档使用

- 预发环境/正式环境切换，以通过如下位置切换API环境

![CleanShot 2025-09-10 at 12 .08.00@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/f9fb7a0f020d4a0e94159d7820783224.png)

- API分两种，一种是需要鉴权（需登录后的token），另一种是直接访问，可以直接访问的GET请求，直接点击Send即可调试，如下👇，比如本站的推荐流rss: /api/rss: https://docs.open-isle.com/openapi/feed

![CleanShot 2025-09-10 at 12 .09.48@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/2afb42e0c96340559dd42854905ca5fc.png)

- 需要登陆的API，比如关注，取消关注，发帖等，则需要提供token，目前在“API与调试”可获取自身token，可点击link看看👉 https://www.open-isle.com/about?tab=api

![CleanShot 2025-09-10 at 12 .11.07@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/74033f1b9cc14f2fab3cbe3b7fe306d8.png)

copy完token之后，粘贴到Bear之后, 即可发送调试， 如下👇，大家亦可自行尝试：https://docs.open-isle.com/openapi/me

![CleanShot 2025-09-10 at 12 .13.00@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/63913fe2e70541a486651e35c723765e.png)

#### OpenAPI文档应用场景

- 方便大部分前端调试的需求，如果有只想做前端/客户端的同学参与本项目，该平台会大大提高效率
- 自动化：有自动化发帖/自动化操作的需求，亦可通过该平台实现或调试
- API文档: https://docs.open-isle.com/openapi
