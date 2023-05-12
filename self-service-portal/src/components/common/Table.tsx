import React from "react";
import { properText } from "../../utils/StringUtils";
import StatusChip from "./StatusChip";

function classNames(...classes: any[]) {
  return classes.filter(Boolean).join(" ");
}

export default function Table({
  title,
  subtext,
  action,
  actionText,
  showRowActions,
  headers,
  data,
  rowActions,
  onRowClick,
  primaryColumnIndex = 0,
}: {
  title: string;
  subtext?: string;
  action?: () => void;
  actionText?: string;
  showRowActions?: (id: string) => boolean;
  headers: string[];
  data: { [key: string]: string }[];
  rowActions?: ((id: string) => React.ReactNode)[];
  onRowClick?: (id: string) => void;
  primaryColumnIndex?: number;
}) {
  return (
    <div className="px-6 lg:px-8">
      <div className="sm:flex sm:items-center">
        <div className="sm:flex-auto">
          <h1 className="text-xl font-semibold text-gray-900">{title}</h1>
          <p className="mt-2 text-sm text-gray-700">{subtext}</p>
        </div>
        <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
          {action && (
            <button
              type="button"
              className="block rounded-md bg-indigo-600 py-1.5 px-3 text-center text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            >
              {actionText}
            </button>
          )}
        </div>
      </div>
      <div className="mt-8 flow-root">
        <div className="-my-2 -mx-6 overflow-x-auto lg:-mx-8">
          <div className="inline-block min-w-full py-2 align-middle">
            <table className="min-w-full divide-y divide-gray-300">
              <thead className="bg-gray-200">
                <tr>
                  {headers.map((header, index) => (
                    <th
                      scope="col"
                      className={classNames(
                        `${
                          index === 0
                            ? "py-3.5 pl-6 pr-3 lg:pl-8"
                            : "px-3 py-3.5"
                        }`,
                        "text-left text-sm font-semibold text-gray-900"
                      )}
                    >
                      {properText(header)}
                    </th>
                  ))}
                  {rowActions && (
                    <th
                      scope="col"
                      className="relative py-3.5 pl-3 pr-6 lg:pr-8"
                    >
                      <span className="sr-only">Actions</span>
                    </th>
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {data.map((item) => (
                  <tr
                    key={item.id}
                    className={
                      onRowClick
                        ? "hover:bg-gray-50 hover:text-gray-900 cursor-pointer bg-white"
                        : "bg-white"
                    }
                    onClick={() => onRowClick && onRowClick(item.id)}
                  >
                    {headers.map((header, index) => (
                      <td
                        className={classNames(
                          index === 0
                            ? "pl-6 pr-3 text-gray-900 lg:pl-8"
                            : "px-3 text-gray-500",
                          index === primaryColumnIndex && "font-medium",
                          "whitespace-nowrap py-4 text-sm"
                        )}
                      >
                        {header === "status" ? (
                          <StatusChip status={item[header] as any} />
                        ) : (
                          item[header]
                        )}
                      </td>
                    ))}
                    {rowActions && showRowActions && showRowActions(item.id) ? (
                      <td className="relative whitespace-nowrap py-4 pl-3 pr-6 text-right text-sm font-medium lg:pr-8">
                        <div className="inline-flex space-x-2">
                          {rowActions.map((action) => action(item.id))}
                        </div>
                      </td>
                    ) : (
                      <div className="w-full bg-white" />
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
