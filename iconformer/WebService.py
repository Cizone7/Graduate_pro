from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import os
from werkzeug.utils import secure_filename
import torch
import torch.nn as nn
from PIL import Image
from torchvision.utils import save_image
from imghdr import what
import models.transformer as transformer
from test import StyTR, test_transform, content_transform, style_transform

app = Flask(__name__)
CORS(app)

UPLOAD_FOLDER = 'uploads'
OUTPUT_FOLDER = 'output'
CONTENT_SIZE = 512
STYLE_SIZE = 512
CROP = True
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

torch.backends.cudnn.deterministic = True 
torch.backends.cudnn.benchmark = False

class Args:
    position_embedding = 'sine'
    hidden_dim = 512
    a = 1.0 

def load_model():
    # 加载VGG模型
    vgg = StyTR.vgg
    vgg.load_state_dict(torch.load('./experiments/vgg_normalised.pth'))
    vgg = nn.Sequential(*list(vgg.children())[:44]).eval()
    
    decoder = StyTR.decoder
    Trans = transformer.Transformer()
    embedding = StyTR.PatchEmbed()

    decoder.load_state_dict(torch.load('experiments/decoder_iter_160000.pth'))
    Trans.load_state_dict(torch.load('experiments/transformer_iter_160000.pth'))
    embedding.load_state_dict(torch.load('experiments/embedding_iter_160000.pth'))
    
    network = StyTR.StyTrans(vgg, decoder, embedding, Trans, Args()).eval().to(device)
    return network

print("Loading model...")
network = load_model()
print("Model loaded!")


@app.route('/stylize', methods=['POST'])
def stylize_image():
    try:
        # 每次请求开始前，清理显存
        torch.cuda.empty_cache()
        if 'content' not in request.files or 'style' not in request.files:
            print("❌ 没有收到文件！")
            return jsonify({'error': '缺少内容或风格图片'}), 400
            
        content_file = request.files['content']
        style_file = request.files['style']
        
        content_path = os.path.join(UPLOAD_FOLDER, secure_filename(content_file.filename))
        style_path = os.path.join(UPLOAD_FOLDER, secure_filename(style_file.filename))
        content_file.save(content_path)
        style_file.save(style_path)

        if not what(content_path) or not what(style_path):
            return jsonify({'error': '文件格式不支持，仅支持图片'}), 400
        
        content_tf = test_transform(CONTENT_SIZE, CROP)
        style_tf = test_transform(STYLE_SIZE, CROP)

        content_img = content_tf(Image.open(content_path).convert("RGB"))
        style_img = style_tf(Image.open(style_path).convert("RGB"))

         # 增加 batch 维度，并转到设备上
        content_img = content_img.unsqueeze(0).to(device)
        style_img = style_img.unsqueeze(0).to(device)
        
        with torch.no_grad():
            output = network(content_img, style_img)[0].cpu()
            
        def get_short_hash(file_stream):
            import hashlib
            hash_md5 = hashlib.md5()
            file_stream.seek(0)
            for chunk in iter(lambda: file_stream.read(4096), b""):
                hash_md5.update(chunk)
            return hash_md5.hexdigest()[:6]

        content_hash = get_short_hash(content_file.stream)
        style_hash = get_short_hash(style_file.stream)

        # 组合成短文件名（示例：c3f7a9_s8d4c1_5k2m.png）
        import uuid
        random_tag = str(uuid.uuid4())[:4]  # 4位随机码
        output_filename = f"c{content_hash}_s{style_hash}_{random_tag}.png"

        # 保存文件
        output_path = os.path.join(OUTPUT_FOLDER, output_filename)
        save_image(output, output_path)

         # 删除上传的原始图片
        os.remove(content_path)
        os.remove(style_path)

        return jsonify({'output_url': f"http://localhost:5000/output/{output_filename}", 'status': 'success'})
        
    except torch.cuda.OutOfMemoryError:
        return jsonify({'error': 'GPU显存不足，请尝试512x512以下尺寸'}), 500
    except Exception as e:
        return jsonify({'error': f'处理失败: {str(e)}'}), 500

@app.route('/output/<filename>')
def get_output(filename):
    try:
        download = request.args.get('download', False, type=bool)
        # 检查文件是否存在
        if not os.path.exists(os.path.join(OUTPUT_FOLDER, filename)):
            return jsonify({'error': '文件不存在'}), 404
        # 使用 download_name 代替 attachment_filename
        return send_from_directory(
            OUTPUT_FOLDER,
            filename,
            as_attachment=download,
            download_name=filename  # 关键修正点
        )
    except Exception as e:
        return jsonify({'error': f'服务器错误: {str(e)}'}), 500
    

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, processes=1)
