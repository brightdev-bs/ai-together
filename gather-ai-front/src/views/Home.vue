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

    <v-row class="mt-5 mr-2 justify-end">
      <v-btn @click="submit">Submit</v-btn>
    </v-row>
  </v-container>
</template>

<script>
 import axios from "axios";

 export default {
   data() {
     return {
       imageUrl: null,
       selectedImg: null,
     }
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
       const formData = new FormData();
       console.log("formData = " + formData);
       formData.append('image', this.selectedImg);

       axios.post('http://localhost:8080/interaction', formData)
         .then(response => {
           console.log('Image uploaded successfully:', response.data);
         })
         .catch(error => {
           console.error('Error uploading image:', error);
         });
     }
   },
 }
</script>
<style>

.max-width-img {
  max-width: 50%;
}
</style>
