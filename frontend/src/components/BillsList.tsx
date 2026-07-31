import { useCallback, useEffect, useRef, useState } from "react";
import { getBills, deleteBill, updateBillStatus } from "../services/billService";
import type { Bill, BillStatus, UtilityType } from "../types";
import { Trash2 } from "lucide-react";
import { Button, LiquidButton } from "./ui/liquid-glass-button";

export default function BillsList({ key: _key }: { key?: number }) {
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(false);
  const [month, setMonth] = useState("");
  const [status, setStatus] = useState<BillStatus | "">("");
  const [utilityType, setUtilityType] = useState<UtilityType | "">("");
  const [showDeleteModal, setShowDeleteModal] = useState<number | null>(null);
  const filtersRef = useRef({ month, status, utilityType });

  useEffect(() => {
    filtersRef.current = { month, status, utilityType };
  }, [month, status, utilityType]);

  const load = useCallback(async () => {
    const filters = filtersRef.current;
    setLoading(true);
    try {
      const data = await getBills({
        month: filters.month || undefined,
        status: filters.status || undefined,
        utilityType: filters.utilityType || undefined,
      });
      setBills(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [_key, load]);

  const onFilter = async (e?: React.FormEvent) => {
    e?.preventDefault();
    await load();
  };

  const onDelete = async (id?: number) => {
    if (!id) return;
    setShowDeleteModal(null);
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
    PAID: "bg-green-500/10 text-green-500",
    UNPAID: "bg-yellow-500/10 text-yellow-500",
    OVERDUE: "bg-red-500/10 text-red-500",
    PARTIAL: "bg-blue-500/10 text-blue-500",
  };

  return (
    <div className="space-y-5">
      <form onSubmit={onFilter} className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <div className="md:col-span-1">
          <label htmlFor="month" className="block text-xs font-medium uppercase tracking-wide text-text-muted">Month</label>
          <input type="month" id="month" value={month} onChange={(e) => setMonth(e.target.value)} className="mt-1 block w-full rounded-xl border border-base-300 bg-base-200/85 px-3 py-2.5 shadow-sm focus:border-primary focus:ring-primary sm:text-sm" />
        </div>
        <div className="md:col-span-1">
          <label htmlFor="status" className="block text-xs font-medium uppercase tracking-wide text-text-muted">Status</label>
          <select id="status" value={status} onChange={(e) => setStatus(e.target.value as BillStatus | "")} className="mt-1 block w-full rounded-xl border border-base-300 bg-base-200/85 px-3 py-2.5 shadow-sm focus:border-primary focus:ring-primary sm:text-sm">
            <option value="">All</option>
            <option value="PAID">PAID</option>
            <option value="UNPAID">UNPAID</option>
            <option value="OVERDUE">OVERDUE</option>
            <option value="PARTIAL">PARTIAL</option>
          </select>
        </div>
        <div className="md:col-span-1">
          <label htmlFor="utility" className="block text-xs font-medium uppercase tracking-wide text-text-muted">Utility</label>
          <select id="utility" value={utilityType} onChange={(e) => setUtilityType(e.target.value as UtilityType | "")} className="mt-1 block w-full rounded-xl border border-base-300 bg-base-200/85 px-3 py-2.5 shadow-sm focus:border-primary focus:ring-primary sm:text-sm">
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
          <LiquidButton type="submit" className="w-full">
            Filter
          </LiquidButton>
        </div>
      </form>

      {loading ? (
        <div className="text-center p-8">
          <p className="text-text-muted">Loading bills...</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-2xl border border-base-300/70">
          <table className="min-w-full divide-y divide-base-300">
            <thead className="bg-base-200/90">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Utility</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Amount</th>
                <th scope="col" className="hidden md:table-cell px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Due Date</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-text-muted uppercase tracking-wider">Status</th>
                <th scope="col" className="relative px-6 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody className="bg-base-100/90 divide-y divide-base-200">
              {bills.map((b) => (
                <tr key={b.id} className="hover:bg-base-200/70 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-text-base">{b.utilityType}</div>
                    <div className="text-sm text-text-muted md:hidden">{b.dueDate ?? "-"}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-text-base">${Number(b.amount)?.toFixed(2) ?? "-"}</td>
                  <td className="hidden md:table-cell px-6 py-4 whitespace-nowrap text-sm text-text-muted">{b.dueDate ?? "-"}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${statusColors[b.status!]}`}>{b.status}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="relative inline-block text-left">
                      <select value={b.status ?? ""} onChange={(e) => onChangeStatus(b.id!, e.target.value as BillStatus)} className="rounded-lg border border-base-300 bg-base-200/85 shadow-sm focus:border-primary focus:ring-primary sm:text-sm">
                        <option value="PAID">PAID</option>
                        <option value="UNPAID">UNPAID</option>
                        <option value="OVERDUE">OVERDUE</option>
                        <option value="PARTIAL">PARTIAL</option>
                      </select>
                      <button onClick={() => setShowDeleteModal(b.id!)} className="ml-2 cursor-pointer rounded-lg p-1 text-red-500 transition-colors hover:bg-red-500/10 hover:text-red-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500/40"><Trash2 size={20}/></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="glass-card p-8 rounded-3xl max-w-sm w-full">
            <h3 className="text-lg font-bold text-text-base">Delete Bill</h3>
            <p className="mt-2 text-sm text-text-muted">Are you sure you want to delete this bill? This action cannot be undone.</p>
            <div className="mt-6 flex justify-end space-x-4">
              <Button variant="outline" onClick={() => setShowDeleteModal(null)}>Cancel</Button>
              <LiquidButton variant="destructive" onClick={() => onDelete(showDeleteModal)}>Delete</LiquidButton>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
