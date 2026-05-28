import BillUpload from "../components/BillUpload";
import BillsList from "../components/BillsList";
import { useState } from "react";

export default function BillsPage() {
  const [key, setKey] = useState(0);
  const onBillUploaded = () => {
    setKey(prev => prev + 1);
  }
  return (
    <div className="space-y-8">
      <div>
        <h1 className="hero-title text-4xl sm:text-5xl font-semibold text-text-base">Utility Bills</h1>
        <p className="text-text-muted mt-2">
          Upload and manage your utility bills.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1">
          <div className="glass-card p-6 rounded-3xl">
            <h2 className="text-xl font-semibold text-text-base mb-4 tracking-tight">
              Upload New Bill
            </h2>
            <BillUpload onBillUploaded={onBillUploaded} />
          </div>
        </div>
        <div className="lg:col-span-2">
          <div className="glass-card p-6 rounded-3xl">
            <h2 className="text-xl font-semibold text-text-base mb-4 tracking-tight">
              Submitted Bills
            </h2>
            <BillsList key={key} />
          </div>
        </div>
      </div>
    </div>
  );
}
