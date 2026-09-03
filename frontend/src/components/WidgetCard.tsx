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

const PIE_COLORS = ["#7c3aed", "#ec4899", "#f59e0b", "#22c55e", "#06b6d4", "#a855f7", "#f472b6", "#84cc16"];

export function WidgetCard({ widget }: { widget: WidgetFieldsFragment }) {
  return (
    <div className="widget-card" data-chart-type={widget.chartType}>
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
          <Line type="monotone" dataKey="value" stroke="#7c3aed" strokeWidth={2} />
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
          <Bar dataKey="value" fill="#7c3aed" />
        </BarChart>
      );
  }
}
