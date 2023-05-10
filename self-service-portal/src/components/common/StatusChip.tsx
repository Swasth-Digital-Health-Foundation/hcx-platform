import { classNames } from "./AppLayout";

export default function StatusChip({ status } : { status: "Pending" | "Approved" | "Rejected" }) {
    return <div className={classNames(
        "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium",
        status === "Pending" ? "bg-yellow-100 text-yellow-800" :
        status === "Approved" ? "bg-green-100 text-green-800" :
        status === "Rejected" ? "bg-red-100 text-red-800" : ""
    )}>
        {status}
        </div>
}