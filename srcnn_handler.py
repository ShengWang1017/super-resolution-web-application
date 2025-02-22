from ts.torch_handler.base_handler import BaseHandler
import torch
from PIL import Image
import numpy as np
import io
from models import SRCNN  # 确保你的 models.py 文件位于同目录或路径正确


class SRCNNHandler(BaseHandler):
    """
    自定义 TorchServe Handler 用于 SRCNN 模型
    """

    def initialize(self, context):
        """
        初始化模型
        """
        self.device = torch.device("cpu")  # 确保在 CPU 上运行
        self.model = SRCNN().to(self.device)

        # 加载模型权重文件
        model_dir = context.system_properties.get("model_dir")
        model_path = f"{model_dir}/srcnn_x4.pth"  # 确保 .pth 文件与 .mar 一起打包
        self.model.load_state_dict(torch.load(model_path, map_location=self.device))
        self.model.eval()

    def preprocess(self, data):
        """
        预处理输入数据，将图像转换为模型可接受的张量
        """
        image = Image.open(io.BytesIO(data[0]["body"])).convert("RGB")  # 从输入读取图像
        image = image.resize((image.width // 4 * 4, image.height // 4 * 4))  # 调整大小确保整除

        # 转换为 Y 通道（灰度图）
        ycbcr = np.array(image).astype(np.float32) / 255.0
        y = ycbcr[..., 0]  # 提取 Y 通道

        # 转为 PyTorch 张量
        y = torch.from_numpy(y).unsqueeze(0).unsqueeze(0).to(self.device)
        return y

    def inference(self, data):
        """
        推理逻辑，将输入数据传递给模型
        """
        with torch.no_grad():
            output = self.model(data).clamp(0.0, 1.0)
        return output

    def postprocess(self, inference_output):
        """
        后处理，将模型输出转换为图像格式
        """
        output = inference_output.squeeze(0).squeeze(0).cpu().numpy() * 255.0
        output = np.clip(output, 0, 255).astype(np.uint8)

        # 返回二进制格式图像
        output_image = Image.fromarray(output)
        buf = io.BytesIO()
        output_image.save(buf, format="JPEG")  # 将结果保存为 JPEG
        buf.seek(0)
        return [buf.getvalue()]
