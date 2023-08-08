# Spring boot를 이용해서 만드는 파일 업로드 서버

## 목표
1. 파일을 multiFormPart로 받아서 저장하고 이력도 남긴다.
2. Linode의 파일 저장소, Linode PostgreSQL DB를 이용해본다.(가능하면..)

## 목표가 아닌것
1. 분할 파일 업로드(대용량 파일을 위한 기능)
2. CDN


---

## 개인적인 확인 사항
1. Linode 와 AWS 비교


---

## 구동 하기전 필수 세팅
1. resources에 credentaial.yml을 추가해야한다.
형태는 다음과 같다.

```yaml
cloud:
  linode:
    bucket: 버킷명
    region: 지역 코드
    credentials:
      accessKey: 엑세스 키
      secretKey: 시크릿 키
```
