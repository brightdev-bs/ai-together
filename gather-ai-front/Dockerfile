FROM node:lts as build-stage
WORKDIR /app

# uri 주소
ARG FRONT_ADDRESS
ARG BACKEND_ADDRESS
# package.json이름을 가진 파일을 모두 workdir폴더로 복사한다.
COPY package*.json ./
RUN npm install
# 현재 폴더를 workdir폴더에 복사한다.
COPY ./ .
# 빌드하기 !
RUN npm run build

FROM nginx as production-stage
RUN mkdir /app
COPY --from=build-stage /app/dist /app
COPY /nginx/prod.conf /etc/nginx/conf.d/default.conf