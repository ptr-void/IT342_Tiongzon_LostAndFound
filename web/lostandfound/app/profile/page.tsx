"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import { Package, ShieldAlert, LogOut, Loader2, Camera, ArrowRight } from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export default function ProfilePage() {
    const [user, setUser] = useState<any>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
    const [reportPage, setReportPage] = useState(1);
    const REPORTS_PER_PAGE = 2;
    const avatarInputRef = useRef<HTMLInputElement>(null);
    const router = useRouter();

    const [myReports, setMyReports] = useState<any[]>([]);
    const [myClaims, setMyClaims] = useState<any[]>([]);
    const [claimsOnMyItems, setClaimsOnMyItems] = useState<any[]>([]);

    const refreshClaims = async (token: string) => {
        try {
            const [myRes, receivedRes] = await Promise.all([
                fetch(`${API_BASE}/claims/mine`, { headers: { Authorization: `Bearer ${token}` } }),
                fetch(`${API_BASE}/claims/received`, { headers: { Authorization: `Bearer ${token}` } }),
            ]);
            if (myRes.ok) setMyClaims(await myRes.json());
            if (receivedRes.ok) setClaimsOnMyItems(await receivedRes.json());
        } catch { }
    };

    useEffect(() => {
        const fetchDashboardData = async () => {
            const token = localStorage.getItem("token");
            if (!token || token === "undefined" || token === "null") {
                router.push("/login");
                return;
            }
            try {
                const userRes = await fetch(`${API_BASE}/users/me`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (!userRes.ok) throw new Error("Failed to fetch user");
                const userData = await userRes.json();
                setUser(userData);

                const itemsRes = await fetch(`${API_BASE}/items`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (itemsRes.ok) {
                    const itemsData = await itemsRes.json();
                    setMyReports(itemsData.filter((i: any) => i.reporterId === userData.userId));
                }

                await refreshClaims(token);
            } catch (err: any) {
                if (err.message === "Failed to fetch user") {
                    localStorage.removeItem("token");
                    router.push("/login");
                } else {
                    toast.error("Failed to load data. Is the server running?");
                }
            } finally {
                setIsLoading(false);
            }
        };
        fetchDashboardData();
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        router.push("/login");
    };

    const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const token = localStorage.getItem("token");
        if (!token) return;
        setIsUploadingAvatar(true);
        try {
            const formData = new FormData();
            formData.append("file", file);
            formData.append("upload_preset", "lost_and_found_unsigned");
            const cloudRes = await fetch("https://api.cloudinary.com/v1_1/defkzzqcs/image/upload", {
                method: "POST",
                body: formData,
            });
            const cloudData = await cloudRes.json();
            if (!cloudData.secure_url) throw new Error("Cloudinary upload failed");
            const avatarUrl = cloudData.secure_url;
            const backendRes = await fetch(`${API_BASE}/users/me/avatar`, {
                method: "PATCH",
                headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
                body: JSON.stringify({ avatarUrl }),
            });
            if (!backendRes.ok) throw new Error("Failed to save avatar");
            setUser((prev: any) => ({ ...prev, avatarUrl }));
            toast.success("Profile picture updated!");
        } catch (err: any) {
            toast.error(err.message || "Failed to upload avatar");
        } finally {
            setIsUploadingAvatar(false);
            if (avatarInputRef.current) avatarInputRef.current.value = "";
        }
    };



    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-[calc(100vh-4rem)]">
                <Loader2 className="h-10 w-10 animate-spin text-rose-800" />
            </div>
        );
    }

    if (!user) return null;

    return (
        <div className="min-h-[calc(100vh-4rem)] relative overflow-hidden">
            <div className="container mx-auto max-w-4xl relative z-10 py-6 px-4">
                <h1 className="text-3xl font-extrabold tracking-tight mb-8 text-slate-900 dark:text-white">My Dashboard</h1>

                <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                    {}
                    <div className="md:col-span-1 space-y-6">
                        <Card className="border-rose-100 dark:border-rose-900 shadow-sm bg-white/90 dark:bg-slate-900/90 backdrop-blur-md">
                            <CardHeader className="text-center pb-2">
                                <div className="relative mx-auto h-24 w-24 mb-4 group">
                                    <div className="h-24 w-24 rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 p-1">
                                        <div className="h-full w-full rounded-full bg-white dark:bg-slate-900 flex items-center justify-center overflow-hidden">
                                            {user.avatarUrl ? (
                                                <img src={user.avatarUrl} alt="Avatar" className="object-cover w-full h-full" />
                                            ) : (
                                                <span className="text-2xl font-bold text-rose-800 dark:text-rose-400 uppercase">{user.username?.[0]}</span>
                                            )}
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => avatarInputRef.current?.click()}
                                        disabled={isUploadingAvatar}
                                        className="absolute inset-0 rounded-full bg-black/50 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity cursor-pointer"
                                        title="Change profile picture"
                                    >
                                        {isUploadingAvatar ? <Loader2 className="h-6 w-6 text-white animate-spin" /> : <Camera className="h-6 w-6 text-white" />}
                                    </button>
                                    <input ref={avatarInputRef} type="file" accept="image/*" onChange={handleAvatarUpload} className="hidden" />
                                </div>
                                <CardTitle className="text-xl capitalize truncate w-full px-2" title={user.username}>{user.username}</CardTitle>
                                <CardDescription className="truncate w-full px-2" title={user.email}>{user.email}</CardDescription>
                                <div className="mt-2 flex items-center justify-center gap-2">
                                    <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-900">
                                        {user.role || "USER"}
                                    </Badge>
                                    {user.warningMarks > 0 && (
                                        <Badge variant="destructive" className="flex items-center gap-1 font-bold">
                                            <ShieldAlert className="h-3.5 w-3.5" />
                                            {user.warningMarks} Warning{user.warningMarks > 1 ? "s" : ""}
                                        </Badge>
                                    )}
                                </div>
                            </CardHeader>
                            <CardContent className="pt-4 space-y-4">
                                <div className="flex justify-between text-sm py-2 border-t border-slate-100 dark:border-slate-800">
                                    <span className="text-slate-500 font-medium">Reports</span>
                                    <span className="font-bold">{myReports.length}</span>
                                </div>
                                <div className="flex justify-between text-sm py-2 border-b border-slate-100 dark:border-slate-800">
                                    <span className="text-slate-500 font-medium">Claims</span>
                                    <span className="font-bold">{myClaims.length}</span>
                                </div>
                                <div className="flex justify-between text-sm py-2 border-b border-slate-100 dark:border-slate-800">
                                    <span className="text-slate-500 font-medium">Claims on My Items</span>
                                    <span className="font-bold">{claimsOnMyItems.length}</span>
                                </div>
                                <Button variant="outline" className="w-full border-rose-200 text-rose-800 hover:bg-rose-50" onClick={handleLogout}>
                                    <LogOut className="mr-2 h-4 w-4" /> Sign Out
                                </Button>
                            </CardContent>
                        </Card>
                    </div>

                    {}
                    <div className="md:col-span-3 space-y-8">
                        {}
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <Card className="bg-white/90 dark:bg-slate-900/90 backdrop-blur border-rose-100 shadow-sm">
                                <CardContent className="p-6 flex items-center gap-4">
                                    <div className="p-3 bg-rose-50 rounded-lg dark:bg-rose-900/20">
                                        <Package className="h-8 w-8 text-rose-700 dark:text-rose-400" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-slate-500">Items Reported</p>
                                        <h3 className="text-2xl font-bold">{myReports.length}</h3>
                                    </div>
                                </CardContent>
                            </Card>
                            <Card className="bg-white/90 dark:bg-slate-900/90 backdrop-blur border-amber-100 shadow-sm">
                                <CardContent className="p-6 flex items-center gap-4">
                                    <div className="p-3 bg-amber-50 rounded-lg dark:bg-amber-900/20">
                                        <ShieldAlert className="h-8 w-8 text-amber-600 dark:text-amber-400" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-slate-500">My Filed Claims</p>
                                        <h3 className="text-2xl font-bold">{myClaims.length}</h3>
                                    </div>
                                </CardContent>
                            </Card>
                        </div>

                        {}
                        <section>
                            <div className="flex items-center justify-between gap-2 mb-4">
                                <div className="flex items-center gap-2">
                                    <Package className="h-6 w-6 text-rose-800 dark:text-rose-400" />
                                    <h2 className="text-xl font-bold text-slate-900 dark:text-white">My Reported Items</h2>
                                </div>
                                {myReports.length > 0 && <span className="text-sm text-slate-500 font-medium">{myReports.length} total</span>}
                            </div>
                            {myReports.length === 0 ? (
                                <Card className="bg-white/50 dark:bg-slate-900/50 border-dashed border-slate-300 dark:border-slate-700">
                                    <CardContent className="py-8 text-center text-muted-foreground">You haven&apos;t reported any lost or found items yet.</CardContent>
                                </Card>
                            ) : (() => {
                                const totalPages = Math.ceil(myReports.length / REPORTS_PER_PAGE);
                                const paged = myReports.slice((reportPage - 1) * REPORTS_PER_PAGE, reportPage * REPORTS_PER_PAGE);
                                return (
                                    <>
                                        <div className="grid gap-4">
                                            {paged.map((report) => (
                                                <Card key={report.id} className="bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-sm border-slate-200 dark:border-slate-800">
                                                    <CardHeader className="py-4">
                                                        <div className="flex justify-between items-center">
                                                            <div>
                                                                <CardTitle className="text-lg">{report.title}</CardTitle>
                                                                <CardDescription>Reported on {new Date(report.createdAt).toLocaleDateString()}</CardDescription>
                                                            </div>
                                                            <Badge variant="outline" className={report.status === "LOST" ? "bg-rose-50 text-rose-700 border-rose-200" : "bg-emerald-50 text-emerald-700 border-emerald-200"}>
                                                                {report.status}
                                                            </Badge>
                                                        </div>
                                                    </CardHeader>
                                                </Card>
                                            ))}
                                        </div>
                                        {totalPages > 1 && (
                                            <div className="flex items-center justify-between mt-4">
                                                <Button variant="outline" size="sm" onClick={() => setReportPage((p) => Math.max(1, p - 1))} disabled={reportPage === 1} className="border-slate-200">← Previous</Button>
                                                <span className="text-sm text-slate-500 font-medium">Page {reportPage} of {totalPages}</span>
                                                <Button variant="outline" size="sm" onClick={() => setReportPage((p) => Math.min(totalPages, p + 1))} disabled={reportPage === totalPages} className="border-slate-200">Next →</Button>
                                            </div>
                                        )}
                                    </>
                                );
                            })()}
                        </section>

                        {}
                        <section>
                            <div className="flex items-center gap-2 mb-4">
                                <ShieldAlert className="h-6 w-6 text-amber-600 dark:text-amber-500" />
                                <h2 className="text-xl font-bold text-slate-900 dark:text-white">My Filed Claims</h2>
                            </div>
                            {myClaims.length === 0 ? (
                                <Card className="bg-white/50 dark:bg-slate-900/50 border-dashed border-slate-300 dark:border-slate-700">
                                    <CardContent className="py-8 text-center text-muted-foreground">You have no active claims.</CardContent>
                                </Card>
                            ) : (
                                <div className="grid gap-4">
                                    {myClaims.map((claim) => (
                                        <Card key={claim.id} className="bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-sm border-slate-200 dark:border-slate-800">
                                            <CardHeader className="py-4">
                                                <div className="flex justify-between items-center gap-4">
                                                    <div className="flex-1 min-w-0">
                                                        <CardTitle className="text-lg truncate">{claim.itemTitle || `Claim #${claim.id}`}</CardTitle>
                                                        <CardDescription>Submitted {new Date(claim.createdAt).toLocaleDateString()}</CardDescription>
                                                    </div>
                                                    <StatusBadge status={claim.status} />
                                                    <Link href={`/claims/${claim.id}`}>
                                                        <Button variant="outline" size="sm" className="shrink-0 gap-2">
                                                            Manage Claim <ArrowRight className="h-4 w-4" />
                                                        </Button>
                                                    </Link>
                                                </div>
                                            </CardHeader>
                                        </Card>
                                    ))}
                                </div>
                            )}
                        </section>

                        {}
                        <section>
                            <div className="flex items-center gap-2 mb-4">
                                <ShieldAlert className="h-6 w-6 text-emerald-600 dark:text-emerald-500" />
                                <h2 className="text-xl font-bold text-slate-900 dark:text-white">Claims on My Posted Items</h2>
                            </div>
                            {claimsOnMyItems.length === 0 ? (
                                <Card className="bg-white/50 dark:bg-slate-900/50 border-dashed border-slate-300 dark:border-slate-700">
                                    <CardContent className="py-8 text-center text-muted-foreground">No one has filed a claim on your posted items yet.</CardContent>
                                </Card>
                            ) : (
                                <div className="grid gap-4">
                                    {claimsOnMyItems.map((claim) => (
                                        <Card key={claim.id} className="bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-sm border-slate-200 dark:border-slate-800">
                                            <CardHeader className="py-4">
                                                <div className="flex justify-between items-center gap-4">
                                                    <div className="flex-1 min-w-0">
                                                        <CardTitle className="text-lg truncate">{claim.itemTitle || `Item #${claim.itemId}`}</CardTitle>
                                                        <CardDescription>Claimed by <strong>{claim.claimantName || "Unknown"}</strong> · {new Date(claim.createdAt).toLocaleDateString()}</CardDescription>
                                                    </div>
                                                    <StatusBadge status={claim.status} />
                                                    <Link href={`/claims/${claim.id}`}>
                                                        <Button variant="outline" size="sm" className="shrink-0 gap-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-950/50 dark:hover:bg-emerald-900/50 dark:border-emerald-800 dark:text-emerald-300">
                                                            Manage Claim <ArrowRight className="h-4 w-4" />
                                                        </Button>
                                                    </Link>
                                                </div>
                                            </CardHeader>
                                        </Card>
                                    ))}
                                </div>
                            )}
                        </section>
                    </div>
                </div>
            </div>
        </div>
    );
}
