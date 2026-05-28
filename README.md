# Utilities Spend Tracker (UST)

UST is a web application that automates the process of managing Water, Electricity, Internet, and Gas bills. Instead of typing numbers into spreadsheets, users can upload PDFs, extract structured data, and view trends through an interactive dashboard.

---

## What It Does

- Upload utility bills in PDF format  
- Parse statement data automatically with Apache Tika  
- Store results securely in a relational database  
- Visualize monthly and yearly spend with charts  
- Export clean data for external analysis  

---

## Core Use Cases

| Feature | Description |
|---------|-------------|
| Bill Upload & Parse | Drag-and-drop PDFs. Extracts totals, usage, and dates automatically. |
| Spend Tracking | Monitor monthly spend and usage for each utility type. |
| Trend Visualization | Interactive charts show cost breakdowns and patterns over time. |
| Data Export | Download parsed records in CSV for reporting or analysis. |

---

## Why UST?

Tracking utilities manually is error-prone and time-consuming. UST provides a single place to consolidate bills, uncover trends, and better understand how household or organizational resources are being consumed. It turns static bills into actionable insights.

---

## Tech Stack

- **Frontend:** React + Axios + Recharts  
- **Backend:** Spring Boot + Apache Tika  
- **Database:** MySQL (H2 for development)  
- **Deployment:** Docker, Maven

---

## 🔗 [Live Site](https://utilities-spend-tracker.vercel.app)
