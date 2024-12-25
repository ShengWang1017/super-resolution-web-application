import os
import torch
import numpy as np
from io import BytesIO
from PIL import Image
from ts.torch_handler.base_handler import BaseHandler
from models import SRCNN
from utils import convert_rgb_to_ycbcr, convert_ycbcr_to_rgb

class SuperResolutionHandler(BaseHandler):
    def __init__(self):
        super(SuperResolutionHandler, self).__init__()
        self.model = None
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    def initialize(self, context):
        self.manifest = context.manifest
        model_dir = context.system_properties.get("model_dir")
        serialized_file = self.manifest['model']['serializedFile']
        model_path = os.path.join(model_dir, serialized_file)

        # Initialize SRCNN model
        self.model = SRCNN().to(self.device)
        self.model.load_state_dict(torch.load(model_path, map_location=self.device))
        self.model.eval()

    def preprocess(self, data):
        """Converts input image to tensor format."""
        image = Image.open(BytesIO(data[0].get("body"))).convert('RGB')

        image_width = (image.width // 3) * 3  # Ensure dimensions are divisible by scale
        image_height = (image.height // 3) * 3
        image = image.resize((image_width, image_height), resample=Image.BICUBIC)

        image = np.array(image).astype(np.float32)
        ycbcr = convert_rgb_to_ycbcr(image)

        y = ycbcr[..., 0]
        y /= 255.0

        y = torch.from_numpy(y).to(self.device)
        y = y.unsqueeze(0).unsqueeze(0)
        return y, ycbcr[..., 1:], image_width, image_height

    def inference(self, data):
        """Performs super-resolution inference on the input tensor."""
        with torch.no_grad():
            output = self.model(data).clamp(0.0, 1.0)
        return output

    def postprocess(self, inference_output):
        """Converts the output tensor back to an image."""
        preds, cbcr, width, height = inference_output
        preds = preds.mul(255.0).cpu().numpy().squeeze(0).squeeze(0)

        output = np.array([preds, cbcr[..., 0], cbcr[..., 1]]).transpose([1, 2, 0])
        output = np.clip(convert_ycbcr_to_rgb(output), 0.0, 255.0).astype(np.uint8)
        output_image = Image.fromarray(output)

        buffer = BytesIO()
        output_image.save(buffer, format="JPEG")
        return [buffer.getvalue()]

    def handle(self, data, context):
        if not data:
            return {"error": "No data provided"}

        # Preprocess input data
        y, cbcr, width, height = self.preprocess(data)

        # Run inference
        preds = self.inference(y)

        # Postprocess output
        result = self.postprocess((preds, cbcr, width, height))
        return result
