import { useEffect, useState } from "react";
import {
  getBills,
  deleteBill,
  updateBillStatus,
} from "../services/billService";
import type { Bill, BillStatus, UtilityType } from "../types";

export default function BillsList() {
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(false);
  const [month, setMonth] = useState("");
  const [status, setStatus] = useState<BillStatus | "">("");
  const [utilityType, setUtilityType] = useState<UtilityType | "">("");

  const load = async () => {
    setLoading(true);
    try {
      const data = await getBills({
        month: month || undefined,
        status: status || undefined,
        utilityType: utilityType || undefined,
      });
      setBills(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onFilter = async (e?: React.FormEvent) => {
    e?.preventDefault();
    await load();
  };

  const onDelete = async (id?: number) => {
    if (!id) return;
    if (!confirm("Delete this bill?")) return;
    await deleteBill(id);
    await load();
  };

  const onChangeStatus = async (id: number, newStatus: BillStatus) => {
    try {
      await updateBillStatus(id, newStatus);
      await load();
    } catch (err) {
      console.error(err);
    }
  };

  const statusColors: { [key in BillStatus]: string } = {
    PAID: "bg-green-100 text-green-800",
    UNPAID: "bg-yellow-100 text-yellow-800",
    OVERDUE: "bg-red-100 text-red-800",
    PARTIAL: "bg-blue-100 text-blue-800",
  };

  return (
    <div>
      <form
        onSubmit={onFilter}
        className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6"
      >
        <div>
          <label htmlFor="month" className="block text-sm font-medium text-gray-700">
            Month
          </label>
          <input
            type="month"
            id="month"
            value={month}
            onChange={(e) => setMonth(e.target.value)}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
          />
        </div>
        <div>
          <label htmlFor="status" className="block text-sm font-medium text-gray-700">
            Status
          </label>
          <select
            id="status"
            value={status}
            onChange={(e) => setStatus(e.target.value as BillStatus | "")}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
          >
            <option value="">All</option>
            <option value="PAID">PAID</option>
            <option value="UNPAID">UNPAID</option>
            <option value="OVERDUE">OVERDUE</option>
            <option value="PARTIAL">PARTIAL</option>
          </select>
        </div>
        <div>
          <label htmlFor="utility" className="block text-sm font-medium text-gray-700">
            Utility
          </label>
          <select
            id="utility"
            value={utilityType}
            onChange={(e) => setUtilityType(e.target.value as UtilityType | "")}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
          >
            <option value="">All</option>
            <option value="ELECTRICITY">ELECTRICITY</option>
            <option value="WATER">WATER</option>
            <option value="GAS">GAS</option>
            <option value="INTERNET">INTERNET</option>
            <option value="SEWER">SEWER</option>
            <option value="OTHER">OTHER</option>
            <option value="PROCESSING_FEE">PROCESSING_FEE</option>
          </select>
        </div>
        <div className="self-end">
          <button
            type="submit"
            className="w-full justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Filter
          </button>
        </div>
      </form>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  ID
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Utility
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Service Month
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Due
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Amount
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Status
                </th>
                <th scope="col" className="relative px-6 py-3">
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {bills.map((b) => (
                <tr key={b.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {b.id}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {b.utilityType}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {b.serviceMonth ?? "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {b.dueDate ?? "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {typeof b.amount === "number" ? (b.amount as number).toFixed(2) : "-"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span
                      className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        statusColors[b.status!]
                      }`}
                    >
                      {b.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <select
                      value={b.status ?? ""}
                      onChange={(e) =>
                        onChangeStatus(b.id!, e.target.value as BillStatus)
                      }
                      className="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                    >
                      <option value="PAID">PAID</option>
                      <option value="UNPAID">UNPAID</option>
                      <option value="OVERDUE">OVERDUE</option>
                      <option value="PARTIAL">PARTIAL</option>
                    </select>
                    <button
                      className="ml-4 text-red-600 hover:text-red-900"
                      onClick={() => onDelete(b.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
