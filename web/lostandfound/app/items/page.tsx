"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardFooter, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import Link from "next/link";
import { Loader2, Search } from "lucide-react";
import { toast } from "sonner";

export default function ItemsPage() {
    const [items, setItems] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [filter, setFilter] = useState("ALL");
    const [categoryFilter, setCategoryFilter] = useState("ALL");
    const [search, setSearch] = useState("");
    const [sortBy, setSortBy] = useState("newest");

    useEffect(() => {
        const fetchItems = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                setIsLoading(false);
                return;
            }
            try {
                const headers: any = {};
                if (token) {
                    headers["Authorization"] = `Bearer ${token}`;
                }

                const response = await fetch("http://localhost:8080/api/items", {
                    headers
                });
                if (response.ok) {
                    const data = await response.json();
                    setItems(data);
                } else {
                    toast.error("Failed to load catalog.");
                }
            } catch (err) {
                console.error("Failed to fetch items", err);
                toast.error("Network error. Backend might be offline.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchItems();
    }, []);

    const filteredItems = items
        .filter(item => {
            if (filter !== "ALL" && item.status !== filter) return false;
            if (categoryFilter !== "ALL" && item.category !== categoryFilter) return false;
            if (search.trim()) {
                const q = search.trim().toLowerCase();
                const matches =
                    item.title?.toLowerCase().includes(q) ||
                    item.description?.toLowerCase().includes(q) ||
                    item.locationDescription?.toLowerCase().includes(q);
                if (!matches) return false;
            }
            return true;
        })
        .sort((a, b) => {
            if (sortBy === "newest") return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
            if (sortBy === "oldest") return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
            if (sortBy === "az") return (a.title || "").localeCompare(b.title || "");
            if (sortBy === "za") return (b.title || "").localeCompare(a.title || "");
            return 0;
        });

    return (
        <div className="min-h-[calc(100vh-4rem)] pt-4 pb-8 relative overflow-hidden">
            <div className="container relative z-10 mx-auto max-w-6xl px-4">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-8 gap-4 bg-white/70 dark:bg-slate-900/70 p-6 rounded-xl border border-rose-100 dark:border-rose-900 shadow-sm backdrop-blur-md">
                    <div>
                        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">Lost &amp; Found Hub</h1>
                        <p className="text-slate-500 mt-1">Browse and find items that have been reported across campus.</p>
                    </div>
                    <Link href="/report">
                        <Button className="bg-rose-900 hover:bg-rose-950 text-white shadow-md">
                            Report an Item
                        </Button>
                    </Link>
                </div>

                <div className="mb-6">
                    <div className="relative mb-4">
                        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
                        <input
                            type="text"
                            placeholder="Search by title, description, or location..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="w-full h-11 pl-10 pr-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-400 transition-all"
                        />
                    </div>

                    <div className="flex flex-col md:flex-row gap-3 justify-between">
                    <div className="flex flex-wrap gap-2">
                            <Button variant="outline" onClick={() => setFilter("ALL")} className={`${filter === 'ALL' ? 'border-amber-400 bg-amber-50 text-amber-800' : 'border-slate-200'}`}>All</Button>
                            <Button variant="outline" onClick={() => setFilter("LOST")} className={`${filter === 'LOST' ? 'border-rose-400 bg-rose-50 text-rose-800' : 'border-slate-200'}`}>Lost</Button>
                            <Button variant="outline" onClick={() => setFilter("FOUND")} className={`${filter === 'FOUND' ? 'border-emerald-400 bg-emerald-50 text-emerald-800' : 'border-slate-200'}`}>Found</Button>
                        </div>

                        <div className="flex gap-2 items-center flex-wrap">
                            <span className="text-sm font-medium text-slate-500">Category:</span>
                            <select
                                className="h-10 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-1 focus:ring-amber-500 dark:bg-slate-900 dark:border-slate-800"
                                value={categoryFilter}
                                onChange={(e) => setCategoryFilter(e.target.value)}
                            >
                                <option value="ALL">All Categories</option>
                                <option value="ELECTRONICS">Electronics</option>
                                <option value="CLOTHING">Clothing</option>
                                <option value="DOCUMENTS">Documents/ID</option>
                                <option value="VALUABLES">Valuables</option>
                                <option value="OTHER">Other</option>
                            </select>

                            <span className="text-sm font-medium text-slate-500">Sort:</span>
                            <select
                                className="h-10 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-1 focus:ring-amber-500 dark:bg-slate-900 dark:border-slate-800"
                                value={sortBy}
                                onChange={(e) => setSortBy(e.target.value)}
                            >
                                <option value="newest">Newest First</option>
                                <option value="oldest">Oldest First</option>
                                <option value="az">Title A → Z</option>
                                <option value="za">Title Z → A</option>
                            </select>
                        </div>
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex justify-center items-center py-32 bg-white/40 backdrop-blur-sm rounded-xl border border-slate-200 dark:border-slate-800">
                        <Loader2 className="h-10 w-10 animate-spin text-rose-800" />
                    </div>
                ) : items.length === 0 ? (
                    <div className="text-center py-32 bg-white/50 backdrop-blur-sm rounded-xl border border-dashed border-slate-300 shadow-sm">
                        <p className="text-slate-600 font-medium mb-4 text-lg">No items have been reported yet.</p>
                        <Link href="/report">
                            <Button variant="outline" className="border-rose-200 text-rose-800 hover:bg-rose-50">Be the first to report</Button>
                        </Link>
                    </div>
                ) : filteredItems.length === 0 ? (
                    <div className="text-center py-20 bg-white/50 backdrop-blur-sm rounded-xl border border-dashed border-slate-300">
                        <p className="text-slate-500 mb-4">No items found matching your filters.</p>
                        <Button variant="outline" onClick={() => { setFilter("ALL"); setCategoryFilter("ALL"); }}>Clear Filters</Button>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                        {filteredItems.map((item) => (
                            <Link href={`/items/${item.id}`} key={item.id} className="block group">
                                <Card className="flex flex-col h-full border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-lg hover:border-amber-300 transition-all bg-white/90 dark:bg-slate-900/90 backdrop-blur-md cursor-pointer">
                                    <CardHeader className="pb-3">
                                        <div className="flex justify-between items-start gap-4">
                                            <CardTitle className="text-xl line-clamp-1 group-hover:text-rose-800 transition-colors">{item.title}</CardTitle>
                                            <StatusBadge status={item.status} />
                                        </div>
                                        <CardDescription className="text-amber-600 dark:text-amber-500 font-semibold text-xs tracking-wider uppercase">
                                            {item.category}
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="flex-1 pb-4">
                                        {item.imagePath ? (
                                            <div className="w-full h-32 mb-4 rounded-lg overflow-hidden bg-slate-100 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
                                                <img src={item.imagePath} alt={item.title} className="w-full h-full object-cover" />
                                            </div>
                                        ) : (
                                            <div className="w-full h-32 mb-4 rounded-lg flex items-center justify-center bg-slate-50 dark:bg-slate-800/50 border border-dashed border-slate-200 dark:border-slate-700">
                                                <div className="text-center">
                                                    <svg className="mx-auto h-8 w-8 text-slate-300 dark:text-slate-600 mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                                    </svg>
                                                    <span className="text-xs font-medium text-slate-400 dark:text-slate-500 block">No image</span>
                                                </div>
                                            </div>
                                        )}
                                        <p className="line-clamp-2 text-sm text-slate-600 dark:text-slate-400">{item.description}</p>
                                        <div className="mt-4 flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300 bg-slate-50 dark:bg-slate-800/50 p-2 rounded-md border border-slate-100 dark:border-slate-800">
                                            <svg className="w-4 h-4 text-rose-800 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.242-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                                            <span className="truncate">{item.locationDescription ? item.locationDescription : "No location description"}</span>
                                        </div>
                                    </CardContent>
                                    <CardFooter className="pt-0 flex flex-col gap-3">
                                        <div className="flex items-center gap-2 w-full border-t border-slate-100 dark:border-slate-800 pt-3">
                                            <div className="h-7 w-7 rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 p-0.5 flex-shrink-0">
                                                <div className="h-full w-full rounded-full bg-white dark:bg-slate-900 flex items-center justify-center overflow-hidden">
                                                    {item.reporterAvatar ? (
                                                        <img src={item.reporterAvatar} alt={item.reporterName} className="object-cover w-full h-full" />
                                                    ) : (
                                                        <span className="text-[10px] font-bold text-rose-800">{item.reporterName?.[0]?.toUpperCase()}</span>
                                                    )}
                                                </div>
                                            </div>
                                            <span className="text-xs font-medium text-slate-600 dark:text-slate-400 truncate">{item.reporterName || "Anonymous"}</span>
                                            {item.reporterWarningMarks > 0 && (
                                                <span className="ml-auto text-[10px] font-bold text-amber-700 dark:text-amber-400 flex items-center gap-0.5 bg-amber-50 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-700/50 px-1.5 py-0.5 rounded flex-shrink-0">
                                                    {item.reporterWarningMarks}
                                                </span>
                                            )}
                                        </div>
                                        <Button variant="outline" className="w-full border-rose-200 text-rose-900 group-hover:bg-rose-50 dark:border-rose-900 dark:text-rose-100 dark:group-hover:bg-rose-900/30">
                                            View Details
                                        </Button>
                                    </CardFooter>
                                </Card>
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
