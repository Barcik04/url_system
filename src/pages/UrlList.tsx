const API_URL = import.meta.env.VITE_API_URL;
import { useEffect, useState } from "react"
import { apiFetch } from "../api"

import CreateUrl from "./CreateUrl"
import DeleteUrlButton from "../components/DeleteUrlButton";
import PatchUrlForm from "../components/PatchUrlForm";
import edit from "../photos/edit.png"


import "../css/UrlList.css"


type UrlDto = {
  longUrl: string;
  code: string;
  createdAt: string;
  expiresAt: string;
  clicks: number;
};

type PageResponse<T> = {
  content: T[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
};


function shorten(url: string, length: number = 20) {
  if (url.length <= length) return url;
  return url.substring(0, length) + "...";
}



function fmtDate(iso: string | null | undefined) {
  if (!iso) return "-";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "-";

  return new Intl.DateTimeFormat("pl-PL", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(d);
}


function getPageItems(current: number, totalPages: number) {
  const last = totalPages - 1;
  if (totalPages <= 7) return [...Array(totalPages)].map((_, i) => i);

  const items: (number | "dots")[] = [];
  const windowStart = Math.max(1, current - 1);
  const windowEnd = Math.min(last - 1, current + 1);

  items.push(0);

  if (windowStart > 1) items.push("dots");

  for (let i = windowStart; i <= windowEnd; i++) items.push(i);

  if (windowEnd < last - 1) items.push("dots");

  items.push(last);

  return items;
}



function UrlList() {
    const [q, setQ] = useState("");
    const [expired, setExpired] = useState(false);
    const [msg, setMsg] = useState("")
    const [pageData, setPageData] = useState<PageResponse<UrlDto> | null>(null);
    const [hoveredCode, setHoveredCode] = useState<string | null>(null);
    const SHORT_BASE = import.meta.env.VITE_API_URL;
    const [open, setOpen] = useState(false);
    const [patchOpen, setPatchOpen] = useState(false);
    const [patchCode, setPatchCode] = useState<string | null>(null);


    const [page, setPage] = useState(0);
    const size = 10;

    const [debouncedQ, setDebouncedQ] = useState(q);
    useEffect(() => {
        const t = setTimeout(() => setDebouncedQ(q), 200);
        return () => clearTimeout(t);
    }, [q]);

    async function displayUrls() {
        try {
            const res = await apiFetch(`/api/v1/show-my-links?page=${page}&size=${size}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }, 
                body: JSON.stringify({
                    q: debouncedQ.trim(), 
                    expired: expired,      
                }),
            });

            if (res.status == 401) {
                throw new Error(`Please log in to access`);
            }

            if (!res.ok) {
                const txt = await res.text();
                throw new Error(`HTTP ${res.status}: ${txt}`);
            }

            const data: PageResponse<UrlDto> = await res.json();
            console.log("UrlList response:", data); 
            setPageData(data);
            setMsg("");
        } catch(e) {
            console.error(e);
            setMsg("Error loading your urls")
        }
    }

    
    useEffect(() => {
        setPage(0);
    }, [debouncedQ, expired]);

    useEffect(() => {
        displayUrls();
    }, [debouncedQ, expired, page]);


    return (
        <div>
            <div className="urlList">
                <div className="searchBar"> 
                    <input className="stringSearch" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search by url"/>
                    <label className="isExpiredCheck">
                        Expired only
                        <input className="isExpiredInput" type="checkbox" checked={expired} onChange={(e) => setExpired(e.target.checked)} />
                    </label>
                
                </div>


                {msg && <p>{msg}</p>}

                {!pageData ? (
                    <p>Loading...</p>
                ) : (
                    <div className="tableAndActions" onMouseLeave={() => setHoveredCode(null)}>
                        <div className="tableWrap">
                        <table className="table">
                            <thead>
                            <tr>
                                <th>longUrl</th>
                                <th>shortUrl</th>
                                <th>createdAt</th>
                                <th>expiresAt</th>
                                <th className="num">clicks</th>
                            </tr>
                            </thead>

                            <tbody>
                            {pageData.content.map((u) => (
                                <tr key={u.code} onMouseEnter={() => setHoveredCode(u.code)}>
                                <td className="cellLong" title={u.longUrl}>{shorten(u.longUrl)}</td>
                                <td className="mono">
                                    <a href={`${SHORT_BASE}/${u.code}`} target="_blank" rel="noreferrer">
                                    {SHORT_BASE}/{u.code}
                                    </a>
                                </td>
                                <td className="mono">{fmtDate(u.createdAt)}</td>
                                <td className="mono">{fmtDate(u.expiresAt)}</td>
                                <td className="num">{u.clicks}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                        </div>

                        <div className="deleteColumn">
                        {pageData.content.map((u) => (
                            <div 
                                key={u.code}   
                                className={`deleteRow ${hoveredCode === u.code ? "show" : ""}`} 
                                onMouseEnter={() => setHoveredCode(u.code)}
                                onMouseLeave={() => setHoveredCode(null)}
                            > 
                                <ul>
                                    <li> <DeleteUrlButton code={u.code} onDeleted={displayUrls} /></li>
                                    <li>
                                        <button
                                            className="patchBtn"
                                            onClick={() => {
                                                setPatchCode(u.code);
                                                setPatchOpen(true);
                                            }}
                                            >
                                            <img src={edit} alt="edit" />
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        ))}
                        </div>
                    </div>
                )}


                            
            {pageData && (
                <div className="paginationBar">
                    <button
                    className="pageBtn"
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={pageData.first}
                    >
                    Prev
                    </button>

                    {getPageItems(pageData.number, pageData.totalPages).map((it, idx) =>
                    it === "dots" ? (
                        <span key={`dots-${idx}`} className="dots">...</span>
                    ) : (
                        <button
                        key={it}
                        className={`pageBtn ${it === pageData.number ? "active" : ""}`}
                        onClick={() => setPage(it)}
                        >
                        {it + 1}
                        </button>
                    )
                    )}

                    <button
                    className="pageBtn"
                    onClick={() => setPage(p => Math.min(pageData.totalPages - 1, p + 1))}
                    disabled={pageData.last}
                    >
                    Next
                    </button>
                </div>
            )}
            </div>
            
            {patchOpen && patchCode && (
                <div className="overlay" onClick={() => { setPatchOpen(false); setPatchCode(null); }}>
                    <div onClick={(e) => e.stopPropagation()}>
                    <PatchUrlForm
                        code={patchCode}
                        onClose={() => { setPatchOpen(false); setPatchCode(null); }}
                        onSuccess={() => {
                        setPatchOpen(false);
                        setPatchCode(null);
                        displayUrls();
                        }}
                    />
                    </div>
                </div>
            )}
        </div>
    )
}

export default UrlList;