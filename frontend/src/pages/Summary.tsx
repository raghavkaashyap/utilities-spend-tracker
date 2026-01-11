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

  useEffect(() => {
    (async () => {
      try {
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
      }
    })();
  }, []);

  return (
    <div className="p-8 bg-gray-50 min-h-screen">
      <h1 className="text-3xl font-bold text-gray-800 mb-8">
        Spending Summary
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-gray-700 mb-4">
            By Utility
          </h2>
          <SpendBarChart data={byUtility} />
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-gray-700 mb-4">
            By Status
          </h2>
          <SpendBarChart data={byStatus} />
        </div>
        <div className="bg-white p-6 rounded-lg shadow-md md:col-span-2">
          <h2 className="text-xl font-semibold text-gray-700 mb-4">
            Monthly Totals
          </h2>
          <MonthlyTrendChart data={monthly} />
        </div>
      </div>
    </div>
  );
}
