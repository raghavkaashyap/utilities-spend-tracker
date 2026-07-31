import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { useTheme } from "../context/useTheme";

interface SpendBarChartProps {
  data: { name: string; value: number }[];
}

const SpendBarChart = ({ data }: SpendBarChartProps) => {
  const { theme } = useTheme();
  const primaryColor = getComputedStyle(document.documentElement).getPropertyValue('--color-primary');

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data} margin={{ top: 5, right: 20, left: -10, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#4b5563' : '#e5e7eb'} />
        <XAxis dataKey="name" tick={{ fill: theme === 'dark' ? '#9ca3af' : '#6b7280' }} />
        <YAxis tick={{ fill: theme === 'dark' ? '#9ca3af' : '#6b7280' }} />
        <Tooltip
          cursor={{ fill: "rgba(129, 140, 248, 0.1)" }}
          contentStyle={{
            backgroundColor: theme === 'dark' ? '#374151' : '#ffffff',
            borderColor: theme === 'dark' ? '#4b5563' : '#e5e7eb',
            color: theme === 'dark' ? '#f9fafb' : '#1f2937',
            borderRadius: '0.5rem',
          }}
        />
        <Bar dataKey="value" fill={primaryColor} radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
};

export default SpendBarChart;
