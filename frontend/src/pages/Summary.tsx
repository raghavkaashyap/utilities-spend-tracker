import { useEffect, useState } from "react";
import {
  getSumByUtility,
  getSumByStatus,
  getMonthlyTotals,
} from "../services/billService";
import SpendBarChart from "../components/SpendBarChart";
import MonthlyTrendChart from "../components/MonthlyTrendChart";

export default function Summary() {
  const [byUtility, setByUtility] = useState<{ name: string; value: number }[]>(
    []
  );
  const [byStatus, setByStatus] = useState<{ name: string; value: number }[]>(
    []
  );
  const [monthly, setMonthly] = useState<{ name: string; value: number }[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const utilityData = await getSumByUtility();
        setByUtility(
          utilityData.map(([name, value]) => ({
            name,
            value,
          }))
        );

        const statusData = await getSumByStatus();
        setByStatus(
          statusData.map(([name, value]) => ({
            name,
            value,
          }))
        );

        const monthlyData = await getMonthlyTotals();
        setMonthly(
          monthlyData.map(([name, value]) => ({
            name,
            value,
          }))
        );
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-full">
        <p className="text-text-muted">Loading summary...</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-text-base">Spending Summary</h1>
        <p className="text-text-muted">
          An overview of your utility spending.
        </p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
        <div className="bg-base-100 p-6 rounded-2xl shadow-lg">
          <h2 className="text-xl font-semibold text-text-base mb-4">
            By Utility
          </h2>
          <SpendBarChart data={byUtility} />
        </div>
        <div className="bg-base-100 p-6 rounded-2xl shadow-lg">
          <h2 className="text-xl font-semibold text-text-base mb-4">
            By Status
          </h2>
          <SpendBarChart data={byStatus} />
        </div>
        <div className="bg-base-100 p-6 rounded-2xl shadow-lg xl:col-span-2">
          <h2 className="text-xl font-semibold text-text-base mb-4">
            Monthly Totals
          </h2>
          <MonthlyTrendChart data={monthly} />
        </div>
      </div>
    </div>
  );
}
