<template>
  <v-row>
    <v-progress-circular
      v-if="loading"
      :rotate="360"
      :size="100"
      :width="15"
      :model-value="stage"
      color="teal"
    >
      {{ stage }}
    </v-progress-circular>
  </v-row>
</template>

<script>
export default {
  props: {
    loading: Boolean,
    stage: Number,
  }
}
</script>

<style scoped>

</style>
