<template>
  <v-container>
    <v-card class="pa-4" @dragover.prevent @drop="handleImageDrop">
      <v-row justify="center">
        <v-col>
          <v-file-input label="Drag & Drop or Select an image" accept="image/*" @change="handleImageUpload" ref="fileInput"></v-file-input>
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-col cols="6">
          <v-img
            v-if="imageUrl"
            :src="imageUrl"
            alt="Uploaded Image"
            class="max-width-img"
          ></v-img>
        </v-col>
      </v-row>
    </v-card>

    <v-text-field
      class="mt-2"
      label="Describe the image"
      variant="solo"
      v-model="command"
    />

    <v-row class="mt-5 mr-2 justify-end">
      <v-btn @click="submit">Submit</v-btn>
    </v-row>

    <ProgressBar
      class="progress-bar-container"
      :loading="loading"
      :stage="stage"
    />
  </v-container>
</template>

<script>

import axios from "axios";
import uuid4 from "uuid4";
import ProgressBar from "@/components/ProgressBar.vue";

export default {
   components: {ProgressBar},
   data() {
     return {
       imageUrl: null,
       selectedImg: null,
       command: '',
       eventSource: null,
       uuid: '',
       stage: 0,
       loading: false,
     }
   },
   mounted() {
     this.uuid = uuid4();
     const eventSource = new EventSource('http://localhost:8080/connect?key=' + this.uuid)
     eventSource.onopen = () => {
       console.log("연결 완료");
     }
     eventSource.addEventListener('stage', e => {
       const log = e.data;
       if(log.startsWith('stage')) {
         this.stage = parseInt(log.substring(log.indexOf("}") + 1)) * 10
       }

       console.log(e);
     });

     this.eventSource = eventSource;
   },
   methods: {
     handleImageUpload(event) {
       const file = event.target.files[0];
       if (file) {
         this.setImage(file);
       }
     },
     handleImageDrop(event) {
       event.preventDefault();
       const file = event.dataTransfer.files[0];
       if (file) {
         this.setImage(file);
       }
     },
     setImage(file) {
       const maxSizeInBytes = 1024 * 1024 * 5; // 5MB
       if (file.size <= maxSizeInBytes) {
         this.imageUrl = URL.createObjectURL(file);
       } else {
         alert('Image size is too large. Please select a smaller image.');
         this.$refs.fileInput.reset();
       }
       this.selectedImg = file;
     },
     submit() {
       // if(this.uuid != null) {
       //   this.$notify({ text: "You tried it few mins ago. Try it after 30 mins", type: 'error'});
       //   return;
       // }
       if(!this.selectedImg) {
         this.$notify({ text: "Please select an scribble image", type: 'error'});
         return;
       }

       this.loading = true;

       const imageData = new FormData();
       imageData.append("key", this.uuid);
       imageData.append("image", this.selectedImg);
       imageData.append("command", this.command);
       axios.post('http://localhost:8080/scribble/process', imageData)
         .catch(e => {
           console.log(e);
         })
     },
   },
 }
</script>
<style>

.max-width-img {
  max-width: 50%;
}

.progress-bar-container {
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000; /* 원하는 Z-인덱스 값을 설정하세요. 다른 컴포넌트보다 높아야 합니다. */
}
</style>

