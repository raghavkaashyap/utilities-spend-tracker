import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { useTheme } from "../context/ThemeContext";

interface MonthlyTrendChartProps {
  data: { name: string; value: number }[];
}

const MonthlyTrendChart = ({ data }: MonthlyTrendChartProps) => {
  const { theme } = useTheme();
  const primaryColor = getComputedStyle(document.documentElement).getPropertyValue('--color-primary');
  const secondaryColor = getComputedStyle(document.documentElement).getPropertyValue('--color-secondary');

  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart data={data} margin={{ top: 5, right: 20, left: -10, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={theme === 'dark' ? '#4b5563' : '#e5e7eb'} />
        <XAxis dataKey="name" tick={{ fill: theme === 'dark' ? '#9ca3af' : '#6b7280' }} />
        <YAxis tick={{ fill: theme === 'dark' ? '#9ca3af' : '#6b7280' }} />
        <Tooltip
          cursor={{ stroke: secondaryColor, strokeWidth: 1 }}
          contentStyle={{
            backgroundColor: theme === 'dark' ? '#374151' : '#ffffff',
            borderColor: theme === 'dark' ? '#4b5563' : '#e5e7eb',
            color: theme === 'dark' ? '#f9fafb' : '#1f2937',
            borderRadius: '0.5rem',
          }}
        />
        <Line
          type="monotone"
          dataKey="value"
          stroke={primaryColor}
          strokeWidth={2}
          dot={{ r: 4, fill: primaryColor }}
          activeDot={{ r: 8, stroke: secondaryColor, strokeWidth: 2, fill: primaryColor }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
};

export default MonthlyTrendChart;
