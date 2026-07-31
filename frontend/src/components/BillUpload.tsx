import { useState } from "react";
import { uploadBill } from "../services/billService";
import { UploadCloud, File as FileIcon, X } from "lucide-react";
import { LiquidButton } from "./ui/liquid-glass-button";
import { getErrorMessage } from "../lib/errors";

export default function BillUpload({ onBillUploaded }: { onBillUploaded: () => void }) {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      setFile(selectedFile);
      setMessage(null);
      setError(null);
    }
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      setError("Please select a PDF file");
      return;
    }
    setLoading(true);
    setMessage(null);
    setError(null);
    try {
      await uploadBill(file);
      setMessage("Successfully uploaded!");
      setFile(null);
      onBillUploaded();
    } catch (err: unknown) {
      setError(getErrorMessage(err, "Upload failed"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div>
        <label
          htmlFor="file-upload"
          className="relative flex flex-col items-center justify-center w-full h-48 border border-dashed rounded-2xl cursor-pointer bg-base-200/80 border-base-300 hover:bg-base-300/70 transition-colors"
        >
          {file ? (
            <div className="text-center">
              <FileIcon className="w-12 h-12 mx-auto text-text-muted" />
              <p className="mt-2 text-sm font-medium text-text-base">{file.name}</p>
              <p className="text-xs text-text-muted">{Math.round(file.size / 1024)} KB</p>
              <button
                type="button"
                onClick={() => setFile(null)}
                className="absolute top-3 right-3 rounded-full bg-red-500 p-1.5 text-white transition-colors hover:bg-red-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500/40"
              >
                <X size={16} />
              </button>
            </div>
          ) : (
            <div className="text-center">
              <UploadCloud className="w-12 h-12 mx-auto text-text-muted" />
              <p className="mt-2 text-sm text-text-muted">
                <span className="font-semibold text-primary">Click to upload</span> or drag and drop
              </p>
              <p className="text-xs text-text-muted">PDF only, up to 10MB</p>
            </div>
          )}
        </label>
        <input id="file-upload" name="file-upload" type="file" className="sr-only" onChange={handleFileChange} accept="application/pdf" />
      </div>

      <LiquidButton
        type="submit"
        disabled={!file || loading}
        className="w-full"
      >
        {loading ? "Uploading..." : "Upload Bill"}
      </LiquidButton>

      {message && <p className="text-sm text-green-500">{message}</p>}
      {error && <p className="text-sm text-red-500">{error}</p>}
    </form>
  );
}
