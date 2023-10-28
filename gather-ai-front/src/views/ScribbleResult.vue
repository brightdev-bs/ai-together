<template>
  <v-card class="pa-4">
    <v-row justify="center">
    </v-row>
    <v-row justify="center">
      <v-col cols="6">
        <v-img
          :src="imagePath"
          alt="Result Image is being made"
          class="max-width-img"
        ></v-img>
      </v-col>
    </v-row>
  </v-card>
</template>

<script>
export default {
  data() {
    return {
      imagePath: '',
    }
  },
  mounted() {
    const filename = this.$route.query.id + '.png'
    this.imagePath = './images/outputs/' + filename;
    // 이미지 생성 되었는지 확인
    const img = new Image();
    img.src = this.imagePath;
    img.onload = function() {

    };
    img.onerror = (e) => {
      console.log(e)
      alert("The result is on the way. Please visit again after few minutes")
    };
  }
}
</script>

<style>
.max-width-img {
  max-width: 50%;
}

</style>
