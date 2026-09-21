# Actioning System

ระบบสำหรับประมูลสินค้า พัฒนาด้วย Spring Boot, PostgreSQL ตาม Layered Architecture

## สมาชิกและ Branch

| สมาชิก | รหัสนักศึกษา | Section | Branch | Feature Owner |
|---|---:|---:|---|---|
| ปุณยวีร์ แทนคำ | 673380282-8 | 01 | `poonywee_6733802828-01` | model designer |
| ปริญญ์นกร อยู่แท้กูล | 673380277-1 | 02 | `parinnakorn_6733802771_02` | |
| พงศพัศ เลบ้านแท่น | 673380283-6 | 01 | `poonywee_6733802828-01` | diagram writer |
| ชนิณทร์ ใจช่วง | 673380264-0 | 01 | `chanin_6733802640_01` | reviewer |
| จิณณวัตร โพธิ์ศรีทอง | 673380263-2 | 01 | `jinnawat_673380163-2` |  |

## Repository Structure

```text
project/   Source code + Configuration
test/   การทดสอบทั้งหมด
doc/    เอกสารทั้งหมดและสไลด์
img/    ไฟล์มัลติมีเดีย
```

## Git Workflow

```text
personal branch -> Pull Request -> develop -> release Pull Request -> main
```

- แต่ละคน Commit และ Push ด้วยบัญชี GitHub ของตนเองเท่านั้น
- ห้ามฝากเพื่อน Commit / Push โดยเด็ดขาด
- ตั้งค่า git config user.name และ user.email ให้ตรงกับบัญชี GitHub ของตนเองก่อนเริ่มงาน
- ทุกคนต้องมี Commit ที่มีความหมาย ไม่น้อยกว่า 15 commits กระจายตลอดช่วงเวลาทำโปรเจค (ห้าม Commit รวดเดียวก่อนส่ง)
- การรวมงานต้องผ่าน Pull Request และมี Reviewer อย่างน้อย 1 คน ในทีม
