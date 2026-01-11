import BillUpload from "../components/BillUpload";
import BillsList from "../components/BillsList";

export default function BillsPage() {
  return (
    <div className="p-8 bg-gray-50 min-h-screen">
      <h1 className="text-3xl font-bold text-gray-800 mb-8">
        Utility Bills
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="md:col-span-1">
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold text-gray-700 mb-4">
              Upload New Bill
            </h2>
            <BillUpload />
          </div>
        </div>
        <div className="md:col-span-2">
          <div className="bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold text-gray-700 mb-4">
              Submitted Bills
            </h2>
            <BillsList />
          </div>
        </div>
      </div>
    </div>
  );
}
