# Jakarta EE 11 正式发布：Java 企业开发的新时代

在全球 Java 开发者的期待中，Jakarta EE 11 于近日正式发布。这一版本标志着企业级 Java 开发框架进入了全新的阶段，它不仅带来了更强大的功能支持，还为开发者提供了更加高效、现代化的开发体验。
截止发稿时, Jakarta Platform 代码库已经加上了正式版本 Tag，这标志着继 Core Profile, Web Profile 发布后，Jakarta EE 11 规范所有版本已经完成。

## 持续进化的 Jakarta EE

自 Eclipse 基金会接手 Java EE 的开发以来，Jakarta EE 在保持稳定性的基础上不断推陈出新：

- **Jakarta EE 8 和 9**：主要完成了从 `javax` 到 `jakarta` 命名空间的迁移，以规避 Java 商标带来的法律风险。
- **Jakarta EE 10**：引入了全新的 Core Profile， 强化 CDI 的核心地位，为生态系统注入了活力。Spring 6 已经向这一版本对齐。
- **Jakarta EE 11**：在开发者体验优化上更进一步，同时将 Java 17 设为基础版本，并支持 Java 21 的运行时环境。正在开发的 Spring 7 会跟进这一版本。

## 亮点抢先看

### 新规范：Jakarta Data 闪亮登场

作为 Jakarta EE 11 的重磅新增内容，**Jakarta Data** 专注于数据处理领域。经过两年的孵化和开发，这一规范终于正式加入 Jakarta EE 的生态体系，填补了企业级 Java 开发在数据处理方面的空白。

更多详情请访问 [Jakarta Data 规范文档](https://jakarta.ee/specifications/data/)。

### Java `record` 类型全面支持

Jakarta EE 11 将 Java 的 `record` 类型提升为一等公民，大大简化了数据建模工作。相关规范如 Persistence、Faces、Expression Language 和 Validation 均已支持这一特性。

### 主要规范更新

- **CDI 4.1**：引入了在 `@Produces` 使用 `@Priority` 的能力。
- **Concurrency 3.1**：支持 Java 21 的虚拟线程和 Reactive Streams。
- **REST 4.0**：新增 JSON Merge Patch 功能。
- **Security 4.0**：引入基于内存的 `IdentityStore` 实现，并支持多认证机制。

### 规范清理与优化

- `ManagedBean` 规范已被移除，所有使用 `ManagedBean` 的代码请务必迁移到 CDI。
- 基于 SOAP 的 *Web Service* 规范被标记为废弃，不再推荐在新项目中使用。

## 社区驱动的未来

作为一个完全由社区驱动的项目，Jakarta EE 的未来充满了可能性。一些正在讨论中的特性，包括 Jakarta RPC，Jakarta NoSQL 和 Jakarta MVC，未来有望成为 Jakarta EE 家族新成员。

## 加入 Jakarta EE 的发展之旅

Jakarta EE 11 的发布不仅是企业级 Java 的一小步，更是开发者社区的一大步。更多 Jakarta EE 信息，请关注：
* 官方网站：[https://jakarta.ee](https://jakarta.ee)
* JakartaOne：[https://jakartaone.org](https://jakartaone.org)，敬请关注 [JakartaONE 2025 LiveStream](https://jakartaone.org/2025/) 和 [中文社区专场](https://jakartaone.org/2025/chinese/)
* Slack: JakartaEE Development

如果你也对 Jakarta EE 的未来充满期待，不妨参与官方的 [开发者调查问卷](https://www.surveymonkey.com/r/TanjaJakartaEE)，为 Jakarta EE 的未来添砖加瓦！
