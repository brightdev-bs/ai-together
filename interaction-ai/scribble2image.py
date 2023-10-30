from share import *
import config

import cv2
import einops
import gradio as gr
import numpy as np
import torch
import random
import time
import uuid
import requests
import sys

from datetime import datetime
from pytorch_lightning import seed_everything
from annotator.util import resize_image, HWC3
from cldm.model import create_model, load_state_dict
from cldm.ddim_hacked import DDIMSampler
from PIL import Image

model = create_model('ai-together/interaction-ai/models/cldm_v15.yaml').cpu()
model.load_state_dict(load_state_dict('ai-together/interaction-ai/models/control_sd15_scribble.pth', location='cuda'))
model = model.cuda()
ddim_sampler = DDIMSampler(model)


def process(input_image, prompt, a_prompt, n_prompt, num_samples, image_resolution, ddim_steps, guess_mode, strength, scale, seed, eta):
    with torch.no_grad():
        img = resize_image(HWC3(input_image), image_resolution)
        H, W, C = img.shape

        detected_map = np.zeros_like(img, dtype=np.uint8)
        detected_map[np.min(img, axis=2) < 127] = 255

        control = torch.from_numpy(detected_map.copy()).float().cuda() / 255.0
        control = torch.stack([control for _ in range(num_samples)], dim=0)
        control = einops.rearrange(control, 'b h w c -> b c h w').clone()

        if seed == -1:
            seed = random.randint(0, 65535)
        seed_everything(seed)


        if config.save_memory:
            model.low_vram_shift(is_diffusing=False)


        cond = {"c_concat": [control], "c_crossattn": [model.get_learned_conditioning([prompt + ', ' + a_prompt] * num_samples)]}
        un_cond = {"c_concat": None if guess_mode else [control], "c_crossattn": [model.get_learned_conditioning([n_prompt] * num_samples)]}
        shape = (4, H // 8, W // 8)

        if config.save_memory:
            model.low_vram_shift(is_diffusing=True)


        model.control_scales = [strength * (0.825 ** float(12 - i)) for i in range(13)] if guess_mode else ([strength] * 13)  # Magic number. IDK why. Perhaps because 0.825**12<0.01 but 0.826**12>0.01
        samples, intermediates = ddim_sampler.sample(ddim_steps, num_samples,
                                                     shape, cond, verbose=False, eta=eta,
                                                     unconditional_guidance_scale=scale,
                                                     unconditional_conditioning=un_cond)

        if config.save_memory:
            model.low_vram_shift(is_diffusing=False)

        x_samples = model.decode_first_stage(samples)
        x_samples = (einops.rearrange(x_samples, 'b c h w -> b h w c') * 127.5 + 127.5).cpu().numpy().clip(0, 255).astype(np.uint8)

        results = [x_samples[i] for i in range(num_samples)]
    return results


BASE_DIR = 'ai-together/gather-ai-front/dist/images/inputs/'
SAVE_DIR = 'ai-together/gather-ai-front/dist/images/outputs/'
def process_image(filename, prompt):

    input = Image.open(BASE_DIR + filename)
    image = np.array(input)

    num_samples = 1
    image_resolution = 512
    strength = 0.8
    guess_mode = False
    ddim_steps = 10
    scale = 6.0
    seed = random.seed(int(time.time()))
    eta = 0.0
    a_prompt = 'best quality, extremely detailed'
    n_prompt = 'longbody, lowres, bad anatomy, bad hands, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality'

    results = process(image, prompt, a_prompt, n_prompt, num_samples, image_resolution, ddim_steps, guess_mode, strength, scale, seed, eta)
    for i, image in enumerate(results):
        pil_image = Image.fromarray(image)
        file_path = SAVE_DIR + str(filename) + '.png'
        pil_image.save(file_path)

if __name__ == "__main__":
    arguments = sys.argv[1:]
    if len(arguments) == 2:
        image_file = arguments[0]
        command = arguments[1]
        process_image(image_file, command)
        print("end")
    else:
        print("Arguments are not enough")
