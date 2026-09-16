# ICON Lab / IconFormer

简体中文说明 — 图标管理与风格迁移平台（Transformer + CNN）

## 简介
ICON Lab 是一个图标管理与风格迁移平台样例工程，包含：
- 基于 Transformer + CNN 的风格迁移模型（训练与推理代码在 `iconformer/`）。
- 一个简单的 Flask WebService（`iconformer/WebService.py`）用于对外提供图片风格迁移 API。
- 一个前端/图标管理相关目录 `ICON_LAB/`（依赖 iconify 等前端库，用于展示/管理图标资源）。
项目支持管理员与普通用户角色（共享登录页），并包含上传/下载/收藏等基础功能（前端实现位于 ICON_LAB）。

## 主要特性
- 图像风格迁移：将“风格图片”的风格迁移到“内容图片”上（支持批量处理与单张推理）。
- 训练与评估脚本：`train.py` 用于训练，`test.py` 用于本地推理/批处理生成。
- Web API：`WebService.py` 提供 `/stylize` 接口，接收 content/style 文件并返回输出图片 URL。
- 前端（ICON_LAB）整合 iconify、svg 转换等工具（依赖列在 `ICON_LAB/package.json` 中）。

## 仓库结构（重要部分）
```
README.md
ICON_LAB/             # 前端资源（iconify 等依赖）
  package.json
iconformer/           # 风格迁移模型代码与服务
  train.py            # 训练脚本
  test.py             # 本地推理/批量处理脚本
  WebService.py       # Flask Web 服务，用于接收上传并返回输出
  models/             # 模型定义（Transformer / StyTR）
  util/               # 工具函数
  test_image/         # 默认的示例 content/style 图片目录
experiments/          # 训练/推理所需的模型权重（不在仓库中）
```

## 快速开始（本地运行）
下列命令假设你在类 Unix 环境并使用 Python 3.8+。

1) 克隆仓库并进入目录
```bash
git clone https://github.com/Cizone7/Graduate_pro.git
cd Graduate_pro
```

2) 创建虚拟环境并安装 Python 依赖（需要根据你的环境选择 GPU/CPU 版 torch）
- 建议依赖（根据代码导入推断）：
  - torch, torchvision
  - pillow (PIL)
  - tensorboardX
  - flask, flask-cors, werkzeug
  - numpy, matplotlib, tqdm
安装示例（示意）：
```bash
python -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install torch torchvision pillow tensorboardX flask flask-cors numpy matplotlib tqdm
```
注意：根据你的 CUDA 版本，选择合适的 torch 安装命令（见官方安装说明）。

3) 获取必要的数据与预训练模型
- README 原有链接（保留，点击访问）：
  - 数据库数据：https://www.alipan.com/s/vzzaVj7jHEe
  - natapp：https://www.alipan.com/s/HHYqN6zvehX
  - 图标数据集（百度网盘）：https://pan.baidu.com/s/12-t2Cte8hu6gvn2piWZAeA?pwd=4i7a
- 预训练 VGG（train.py / test.py / WebService.py 使用路径 `./experiments/vgg_normalised.pth`）
- 训练产出模型（示例路径）：
  - experiments/decoder_iter_160000.pth
  - experiments/transformer_iter_160000.pth
  - experiments/embedding_iter_160000.pth
请把这些文件放到 `experiments/` 目录，或修改脚本参数以指定其它路径。

4) 训练（示例）
```bash
python iconformer/train.py \
  --content_dir ./datasets/train2014 \
  --style_dir ./datasets/Images \
  --vgg ./experiments/vgg_normalised.pth \
  --save_dir ./experiments \
  --log_dir ./logs \
  --batch_size 8 \
  --max_iter 160000
```
train.py 中有更多参数（学习率、权重、位置编码类型等），可通过 `--help` 查看。

5) 本地推理 / 批量处理（test.py）
```bash
python iconformer/test.py \
  --content test_image/content/example.jpg \
  --style test_image/style/style.jpg \
  --output output \
  --vgg ./experiments/vgg_normalised.pth \
  --decoder_path ./experiments/decoder_iter_160000.pth \
  --Trans_path ./experiments/transformer_iter_160000.pth \
  --embedding_path ./experiments/embedding_iter_160000.pth
```
默认会把结果保存到 `output/`。

6) 运行 Web 服务（提供 REST API）
```bash
python iconformer/WebService.py
```
服务监听默认 0.0.0.0:5000。API: POST /stylize，表单字段 `content` 和 `style`（文件上传）。示例 curl：
```bash
curl -X POST -F "content=@content.jpg" -F "style=@style.jpg" http://localhost:5000/stylize
```
成功返回类似：
```json
{ "output_url": "http://localhost:5000/output/cxxxx_syyyy_zzzz.png", "status": "success" }
```
下载输出图像：GET /output/<filename>?download=true

注意：如果 GPU 显存不足，WebService 会返回 GPU OOM 提示（建议使用较小尺寸，如 256 或 512 以下）。

7) 前端（ICON_LAB）
ICON_LAB 目录包含前端依赖（见 package.json）。步骤示例：
```bash
cd ICON_LAB
npm install
# 启动方式取决于项目 scripts，请在 package.json 中添加或运行你常用的 dev server（如 npm run dev / npm start）
```
package.json 中列出的主要前端依赖：@iconify/iconify、@iconify/vue2、axios、canvg、html-to-image 等。

## 配置与参数（要点）
- 训练与推理默认图像尺寸：train.py 使用 Resize 512 -> RandomCrop 256；test.py/WebService 中默认 size=512，可通过脚本参数调整。
- WebService 会临时保存上传文件在 `uploads/` 并把生成结果放在 `output/`，成功后会删除上传原图。

## 常见问题
- GPU 显存不足：在 WebService 中会捕获并提示；可尝试减小输入分辨率或在推理时使用 CPU（性能下降）。
- 模型文件缺失：请确保 `experiments/` 下包含 vgg 与训练好的 transformer/decoder/embedding 权重。

## 许可与贡献
若希望我帮你把这个 README 提交为 repo 的 README.md（commit / PR），回复我“提交 README”并说明是否在新分支上打开 PR，我可以为你执行更新（或把 README 作为 patch 给你复制粘贴）。

---

如果你需要，我可以：
- 把上面的 README.md 直接提交到仓库（创建分支 + 打开 PR）。
- 生成一个 requirements.txt 与前端启动脚本示例（package.json scripts）。
- 为 WebService 添加更健壮的错误日志与配置项（例如模型路径、host、port、最大分辨率等）。
