"use client";

import { useParams } from "next/navigation";
import { useState, useEffect } from "react";
import dynamic from "next/dynamic";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import Link from "next/link";
import { ArrowLeft, Loader2, MessageCircle, ShieldCheck } from "lucide-react";

const MapViewer = dynamic(() => import("@/features/items/components/MapViewer"), { ssr: false });

export default function ItemDetailsPage() {
    const params = useParams();
    const id = params.id;

    const [item, setItem] = useState<any>(null);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchItem = async () => {
            try {
                const token = localStorage.getItem("token");
                const [response, meResponse] = await Promise.all([
                    fetch(`http://localhost:8080/api/items/${id}`),
                    token ? fetch("http://localhost:8080/api/users/me", { headers: { Authorization: `Bearer ${token}` } }) : Promise.resolve(null)
                ]);
                if (response.ok) {
                    const data = await response.json();
                    setItem(data);
                } else {
                    console.error("Item not found");
                }
                if (meResponse?.ok) {
                    const me = await meResponse.json();
                    setCurrentUserId(me.userId);
                }
            } catch (error) {
                console.error(error);
            } finally {
                setIsLoading(false);
            }
        };

        if (id) fetchItem();
    }, [id]);

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-[calc(100vh-4rem)]">
                <Loader2 className="h-10 w-10 animate-spin text-rose-800" />
            </div>
        );
    }

    if (!item) {
        return (
            <div className="flex flex-col justify-center items-center h-[calc(100vh-4rem)]">
                <h2 className="text-2xl font-bold mb-4">Item Not Found</h2>
            </div>
        );
    }

    const isOwnItem = item.reporterId === currentUserId;
    const reporterAvatar = item.reporterAvatar || item.reporterAvatarUrl || item.avatarUrl;

    return (
        <div className="min-h-[calc(100vh-4rem)] px-4 py-6 relative overflow-hidden">
            <div className="container relative z-10 mx-auto max-w-3xl">
                <Link href="/items" className="inline-flex items-center text-sm font-medium text-rose-800 hover:text-rose-900 mb-6 bg-white/50 dark:bg-slate-900/50 px-3 py-1.5 rounded-full backdrop-blur-sm border border-rose-100 dark:border-rose-900/30 transition-colors">
                    <ArrowLeft className="mr-2 h-4 w-4" /> Back to Catalog
                </Link>

                <Card className="bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-xl border-slate-200 dark:border-slate-800 overflow-hidden">
                    <CardHeader className="border-b border-slate-100 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50">
                        <div className="flex justify-between items-start gap-4">
                            <div>
                                <CardTitle className="text-3xl font-extrabold text-slate-900 dark:text-white leading-tight">{item.title}</CardTitle>
                                <CardDescription className="text-amber-600 dark:text-amber-500 font-bold tracking-wider uppercase mt-2 text-sm">
                                    {item.category}
                                </CardDescription>
                            </div>
                            <StatusBadge status={item.status} className="text-sm px-3 py-1 font-semibold" />
                        </div>
                    </CardHeader>

                    <CardContent className="p-0">
                        <div className="p-6 space-y-8">
                            {}
                            {item.imagePath ? (
                                <div className="h-64 w-full rounded-xl overflow-hidden shadow-sm">
                                    <img src={item.imagePath} alt={item.title} className="w-full h-full object-cover" />
                                </div>
                            ) : (
                                <div className="bg-slate-100 dark:bg-slate-800 h-64 w-full rounded-xl flex items-center justify-center border border-dashed border-slate-300 dark:border-slate-700">
                                    <span className="text-slate-500 font-medium">Image not available</span>
                                </div>
                            )}

                            <div className="grid md:grid-cols-3 gap-8">
                                <div className="md:col-span-2 space-y-2">
                                    <h3 className="font-bold text-lg text-slate-900 dark:text-white flex items-center gap-2">
                                        <svg className="w-5 h-5 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                                        Description
                                    </h3>
                                    <p className="text-slate-700 dark:text-slate-300 leading-relaxed bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg border border-slate-100 dark:border-slate-800">
                                        {item.description}
                                    </p>
                                </div>
                                <div className="space-y-4">
                                    <div>
                                        <p className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-2">Reported By</p>
                                        <div className="flex items-center gap-2.5">
                                            <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 p-0.5 flex-shrink-0">
                                                <div className="h-full w-full rounded-full bg-white dark:bg-slate-900 flex items-center justify-center overflow-hidden">
                                                    {reporterAvatar ? (
                                                        <img src={reporterAvatar} alt={item.reporterName} className="object-cover w-full h-full" />
                                                    ) : (
                                                        <span className="text-xs font-bold text-rose-800">{item.reporterName?.[0]?.toUpperCase()}</span>
                                                    )}
                                                </div>
                                            </div>
                                            <div className="flex flex-col gap-0.5">
                                                <p className="font-medium text-slate-900 dark:text-white leading-tight">{item.reporterName || "Anonymous"}</p>
                                                {item.reporterWarningMarks > 0 && (
                                                    <span className="flex items-center gap-1 text-[10px] font-bold text-amber-700 dark:text-amber-400 bg-amber-50 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-700/50 px-1.5 py-0.5 rounded w-fit">
                                                        {item.reporterWarningMarks} Warning{item.reporterWarningMarks > 1 ? 's' : ''}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                    <div>
                                        <p className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-1">Date</p>
                                        <p className="font-medium text-slate-900 dark:text-white">{new Date(item.createdAt).toLocaleDateString()}</p>
                                    </div>
                                </div>
                            </div>

                            <div className="space-y-3">
                                <h3 className="font-bold text-lg text-slate-900 dark:text-white flex items-center gap-2">
                                    <svg className="w-5 h-5 text-rose-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.242-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                                    Location Details
                                </h3>
                                <p className="text-slate-700 dark:text-slate-300 font-medium">{item.locationDescription}</p>

                                <div className="mt-4 border-2 border-slate-200 dark:border-slate-700 rounded-xl overflow-hidden shadow-sm h-[300px]">
                                    {(item.locationLat && item.locationLng) ? (
                                        <MapViewer lat={item.locationLat} lng={item.locationLng} />
                                    ) : (
                                        <div className="flex h-full items-center justify-center bg-slate-100 dark:bg-slate-800">
                                            <span className="text-slate-500">No map coordinates available</span>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>

                        {}
                        {!isOwnItem && (
                        <div className="bg-slate-50 dark:bg-slate-900/50 p-6 border-t border-slate-100 dark:border-slate-800 flex flex-col sm:flex-row justify-end gap-3">
                            {item.reporterId && currentUserId && (
                                <Link href={`/chat/${item.reporterId}?itemId=${item.id}`}>
                                    <Button variant="outline" className="w-full sm:w-auto border-amber-200 text-amber-700 hover:bg-amber-50">
                                        <MessageCircle className="mr-2 h-4 w-4" /> Message Reporter
                                    </Button>
                                </Link>
                            )}
                            <Link href={`/claims/new?itemId=${item.id}`}>
                                <Button className="w-full sm:w-auto bg-rose-900 text-white hover:bg-rose-950">
                                    <ShieldCheck className="mr-2 h-4 w-4" /> File Claim
                                </Button>
                            </Link>
                        </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
