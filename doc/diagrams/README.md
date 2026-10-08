# Diagrams

ทุกแผนภาพเขียนด้วย [Mermaid](https://mermaid.js.org/) ภายในไฟล์ Markdown — GitHub แสดงผลให้อัตโนมัติ และแก้ไขเป็น text ได้ง่ายเมื่อโค้ดเปลี่ยน
สร้างจากโค้ดจริงใน `code/src/main/java/com/example/project/` (ณ branch `pongsapat_6733802836_01`)

| ข้อกำหนดข้อ 9.1 | ไฟล์ |
|---|---|
| Use Case Diagram + Use Case Description | [01-use-case.md](01-use-case.md) |
| Domain Model / Conceptual Class Diagram | [02-domain-model.md](02-domain-model.md) |
| Class Diagram (แสดงตำแหน่ง Design Pattern) | [03-class-diagram.md](03-class-diagram.md) |
| Sequence Diagram (4 scenario) | [04-sequence-diagrams.md](04-sequence-diagrams.md) |
| Activity Diagram (2 รูป) | [05-activity-diagram.md](05-activity-diagram.md) |
| ER Diagram / Database Schema | [06-er-diagram.md](06-er-diagram.md) |
| Component Diagram & Deployment Diagram | [07-component-deployment.md](07-component-deployment.md) |
| State Diagram (Bidding, Payment, BidAction) | [08-state-diagram.md](08-state-diagram.md) |

## ส่งออกเป็นรูปภาพ (ถ้าต้องการใส่สไลด์ / `img/`)

```bash
npx -p @mermaid-js/mermaid-cli mmdc -i doc/diagrams/08-state-diagram.md -o img/state-diagram.png
```

`mmdc` รองรับไฟล์ `.md` และจะ export ทุกบล็อก ```` ```mermaid ```` เป็นไฟล์แยกให้
