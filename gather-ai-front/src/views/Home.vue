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

    <v-text-field
      v-model="email"
      :rules="emailRules"
      label="Email (We send result to your email)"
      variant="solo"/>

    <v-row class="mt-2 mr-2 justify-end">
      <v-btn :disabled="isButtonDisabled" @click="submit">Submit</v-btn>
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
       email: '',
       command: '',
       isButtonDisabled: false,
     }
   },
   computed: {
     emailRules() {
       return [
         (v) => !!v || 'Email is required',
         (v) => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(v) || 'Invalid email address',
       ]
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
       if(!this.selectedImg) {
         this.$notify({ text: "Please select an scribble image", type: 'error'});
         return;
       }

       if(!this.email || !this.validateEmail()) {
         this.$notify({ text: "Enter your exact email to get the result", type: 'error'});
         return;
       }

       const formData = new FormData();
       formData.append("image", this.selectedImg);
       formData.append("command", this.command);
       formData.append("email", this.email);

       axios.post('http://localhost:8080/interaction', formData)
         .then(() => {
           this.isButtonDisabled = true;
           setTimeOut(() => {
             this.isButtonDisabled = false;
           }, 180000);
           alert("Check your email. The result will be shown in 10 mins");
         })
         .catch(() => {
           this.$notify({ text: "Image upload is failed. Try again", type: 'error'});
         });
     },
     validateEmail() {
       const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
       return emailRegex.test(this.email);
     },
   },
 }
</script>
<style>

.max-width-img {
  max-width: 50%;
}
</style>
