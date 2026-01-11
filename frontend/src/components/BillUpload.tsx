import { useState } from "react";
import { uploadBill } from "../services/billService";

export default function BillUpload() {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      setMessage("Please select a PDF file");
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const bill = await uploadBill(file);
      setMessage(
        `Uploaded bill id=${bill.id} (${bill.utilityType ?? "unknown"})`
      );
      setFile(null);
      (document.getElementById("file-input") as HTMLInputElement | null)?.value &&
        ((document.getElementById("file-input") as HTMLInputElement).value =
          "");
    } catch (err: any) {
      setMessage(err?.response?.data?.message ?? err?.message ?? "Upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <form onSubmit={onSubmit}>
        <div className="flex flex-col gap-4">
          <label htmlFor="file-input" className="sr-only">
            Choose file
          </label>
          <input
            id="file-input"
            type="file"
            accept="application/pdf"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
          />
          <button
            disabled={loading}
            className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-blue-300"
          >
            {loading ? "Uploading..." : "Upload"}
          </button>
        </div>
      </form>
      {message && (
        <p
          className={`mt-4 text-sm ${
            message.startsWith("Uploaded") ? "text-green-600" : "text-red-600"
          }`}
        >
          {message}
        </p>
      )}
    </div>
  );
}
