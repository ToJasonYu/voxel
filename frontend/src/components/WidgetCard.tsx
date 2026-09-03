import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { WidgetFieldsFragment } from "../generated/graphql";

const ACCENT = "#3b5bdb";
const PIE_COLORS = ["#3b5bdb", "#6b7280", "#16a34a", "#d97706", "#0891b2", "#9333ea"];

export function WidgetCard({ widget }: { widget: WidgetFieldsFragment }) {
  return (
    <div className="widget-card">
      <h3>{widget.title}</h3>
      <ResponsiveContainer width="100%" height={240}>
        {renderChart(widget)}
      </ResponsiveContainer>
    </div>
  );
}

function renderChart(widget: WidgetFieldsFragment) {
  switch (widget.chartType) {
    case "LINE":
      return (
        <LineChart data={widget.data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey="value" stroke={ACCENT} strokeWidth={2} />
        </LineChart>
      );
    case "PIE":
      return (
        <PieChart>
          <Pie data={widget.data} dataKey="value" nameKey="label" outerRadius={80} label>
            {widget.data.map((_, index) => (
              <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      );
    case "BAR":
    default:
      return (
        <BarChart data={widget.data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="label" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="value" fill={ACCENT} />
        </BarChart>
      );
  }
}
