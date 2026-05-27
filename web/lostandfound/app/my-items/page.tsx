"use client"

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import { FileCheck, Loader2, Trash2, Edit } from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function MyItemsPage() {
    const [items, setItems] = useState<any[]>([]);
    const [claims, setClaims] = useState<any[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();

    const fetchMyItems = async () => {
        const token = localStorage.getItem("token");
        if (!token) {
            router.push("/login");
            return;
        }

        try {
            const userRes = await fetch("http://localhost:8080/api/users/me", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!userRes.ok) throw new Error("Not logged in");
            const userData = await userRes.json();

            const itemsRes = await fetch("http://localhost:8080/api/items", {
                headers: { "Authorization": `Bearer ${token}` }
            });

            if (itemsRes.ok) {
                const allItems = await itemsRes.json();
                const myItems = allItems.filter((item: any) => item.reporterId === userData.userId);
                myItems.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
                setItems(myItems);
            } else {
                toast.error("Failed to load your items.");
            }

            const claimsRes = await fetch("http://localhost:8080/api/claims/received", {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (claimsRes.ok) {
                setClaims(await claimsRes.json());
            }
        } catch (err) {
            console.error("Error fetching items", err);
            toast.error("Failed to load dashboard.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchMyItems();
    }, [router]);

    const handleUpdateStatus = async (itemId: number, newStatus: string) => {
        const token = localStorage.getItem("token");
        try {
            const res = await fetch(`http://localhost:8080/api/items/${itemId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ status: newStatus })
            });

            if (res.ok) {
                toast.success(`Item marked as ${newStatus}`);
                fetchMyItems();
            } else {
                toast.error("Failed to update item status.");
            }
        } catch (err) {
            console.error(err);
            toast.error("An error occurred while updating the item.");
        }
    };

    const handleDelete = async (itemId: number) => {
        if (!confirm("Are you sure you want to delete this post? This action cannot be undone.")) return;

        const token = localStorage.getItem("token");
        try {
            const res = await fetch(`http://localhost:8080/api/items/${itemId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (res.ok) {
                toast.success("Item deleted successfully");
                fetchMyItems();
            } else {
                toast.error("Failed to delete item.");
            }
        } catch (err) {
            console.error(err);
            toast.error("An error occurred while deleting the item.");
        }
    };

    const handleClaimStatus = async (claimId: number, action: "approve" | "reject") => {
        const token = localStorage.getItem("token");
        try {
            const res = await fetch(`http://localhost:8080/api/claims/${claimId}/${action}`, {
                method: "POST",
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (res.ok) {
                toast.success(`Claim ${action === "approve" ? "approved" : "rejected"}.`);
                fetchMyItems();
            } else {
                toast.error(`Failed to ${action} claim.`);
            }
        } catch (err) {
            console.error(err);
            toast.error("An error occurred while updating the claim.");
        }
    };

    if (isLoading) {
        return (
            <div className="flex justify-center flex-col items-center h-[calc(100vh-4rem)]">
                <Loader2 className="h-10 w-10 animate-spin text-rose-800 mb-4" />
                <p className="text-slate-500 font-medium animate-pulse">Loading your dashboard...</p>
            </div>
        );
    }

    return (
        <div className="min-h-[calc(100vh-4rem)] px-4 py-6 relative overflow-hidden">
            <div className="container mx-auto relative z-10 max-w-5xl">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4 bg-white/70 dark:bg-slate-900/70 p-6 rounded-xl border border-rose-100 dark:border-rose-900 shadow-sm backdrop-blur-md">
                    <div>
                        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">My Items Dashboard</h1>
                        <p className="text-slate-500 mt-1">Manage, update status, or delete items you have reported.</p>
                    </div>
                    <Link href="/report">
                        <Button className="bg-rose-900 hover:bg-rose-950 text-white shadow-md">
                            Report New Item
                        </Button>
                    </Link>
                </div>

                {items.length === 0 ? (
                    <Card className="bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-xl border-dashed border-2 border-slate-300 dark:border-slate-800">
                        <CardContent className="flex flex-col items-center justify-center py-20">
                            <div className="p-4 bg-slate-100 dark:bg-slate-800 rounded-full mb-4">
                                <Edit className="h-10 w-10 text-slate-400" />
                            </div>
                            <h3 className="text-xl font-bold text-slate-700 dark:text-slate-300 mb-2">No items reported</h3>
                            <p className="text-slate-500 text-center max-w-md mb-6">
                                You haven&apos;t reported any lost or found items yet. Once you do, they will appear here for you to manage.
                            </p>
                            <Link href="/report">
                                <Button variant="outline" className="border-rose-200 text-rose-800 hover:bg-rose-50">Report an Item Now</Button>
                            </Link>
                        </CardContent>
                    </Card>
                ) : (
                    <div className="grid grid-cols-1 gap-6">
                        {items.map((item) => {
                            const itemClaims = claims.filter((claim) => claim.itemId === item.id);
                            return <Card key={item.id} className="overflow-hidden bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-sm hover:shadow-md transition-all border-slate-200 dark:border-slate-800">
                                <div className="flex flex-col sm:flex-row">
                                <div className="w-full ml-5 sm:w-48 h-48 sm:h-auto shrink-0 bg-slate-100 dark:bg-slate-800 relative">
                                    {item.imagePath ? (
                                        <img src={item.imagePath} alt={item.title} className="w-full h-full object-cover" />
                                    ) : (
                                        <div className="w-full h-full flex flex-col items-center justify-center text-slate-400 p-4 text-center">
                                            <svg className="h-8 w-8 mb-2 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                            </svg>
                                            <span className="text-xs uppercase tracking-wider font-semibold">No Image</span>
                                        </div>
                                    )}
                                </div>

                                <div className="flex-1 flex flex-col p-5">
                                    <div className="flex justify-between items-start gap-4 mb-2">
                                        <div>
                                            <h3 className="text-xl font-bold text-slate-900 dark:text-white leading-tight">
                                                <Link href={`/items/${item.id}`} className="hover:text-amber-600 transition-colors">
                                                    {item.title}
                                                </Link>
                                            </h3>
                                            <p className="text-sm font-medium text-amber-600 dark:text-amber-500 mt-1 uppercase tracking-wider">
                                                {item.category}
                                            </p>
                                        </div>
                                        <StatusBadge status={item.status} className="shrink-0" />
                                    </div>

                                    <p className="text-sm text-slate-600 dark:text-slate-400 line-clamp-2 mt-2 mb-4">
                                        {item.description}
                                    </p>

                                    <div className="mt-auto pt-4 border-t border-slate-100 dark:border-slate-800 flex flex-wrap items-center justify-between gap-4">
                                        <span className="text-xs font-semibold text-slate-400 uppercase tracking-widest">
                                            Posted {new Date(item.createdAt).toLocaleDateString()}
                                        </span>

                                        <div className="flex items-center gap-2 flex-wrap">
                                            <div className="flex bg-slate-100 dark:bg-slate-800 p-1 rounded-md border border-slate-200 dark:border-slate-700">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleUpdateStatus(item.id, 'LOST')}
                                                    disabled={item.status === 'LOST'}
                                                    className={`h-7 px-3 text-xs font-bold ${item.status === 'LOST' ? 'bg-white dark:bg-slate-700 text-rose-700 shadow-sm' : 'text-slate-500 hover:text-slate-700 hover:bg-white/50'}`}
                                                >
                                                    LOST
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleUpdateStatus(item.id, 'FOUND')}
                                                    disabled={item.status === 'FOUND'}
                                                    className={`h-7 px-3 text-xs font-bold ${item.status === 'FOUND' ? 'bg-white dark:bg-slate-700 text-emerald-700 shadow-sm' : 'text-slate-500 hover:text-slate-700 hover:bg-white/50'}`}
                                                >
                                                    FOUND
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleUpdateStatus(item.id, 'RESOLVED')}
                                                    disabled={item.status === 'RESOLVED'}
                                                    className={`h-7 px-3 text-xs font-bold ${item.status === 'RESOLVED' ? 'bg-white dark:bg-slate-700 text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700 hover:bg-white/50'}`}
                                                >
                                                    RESOLVED
                                                </Button>
                                            </div>
                                            <Button
                                                variant="destructive"
                                                size="sm"
                                                className="h-9 w-9 p-0 bg-red-50 text-red-600 hover:bg-red-100 hover:text-red-700 border-red-200 ml-2"
                                                onClick={() => handleDelete(item.id)}
                                                title="Delete Post"
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                     </div>
                                 </div>
                                </div>
                                {itemClaims.length > 0 && (
                                    <div className="border-t border-slate-100 bg-amber-50/60 p-4 dark:border-slate-800 dark:bg-amber-950/10">
                                        <div className="mb-3 flex items-center gap-2 text-sm font-bold text-slate-900 dark:text-white">
                                            <FileCheck className="h-4 w-4 text-amber-600" /> File Claims Submitted for This Item
                                        </div>
                                        <div className="grid gap-3">
                                            {itemClaims.map((claim) => <div key={claim.id} className="rounded-lg border bg-white p-3 text-sm dark:bg-slate-900">
                                                <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                                                    <div>
                                                        <p className="font-semibold">Claimed by {claim.claimantName || "Unknown"}</p>
                                                        <p className="mt-1 text-slate-600 dark:text-slate-400">{claim.proofDescription || "No proof description"}</p>
                                                        {claim.proofImagePath && <a href={claim.proofImagePath} target="_blank" className="mt-1 inline-block text-xs font-semibold text-rose-800 underline">View proof image</a>}
                                                        <div className="mt-1 flex flex-wrap items-center gap-2 text-xs font-semibold text-slate-500">Payment: <StatusBadge status={claim.paymentStatus || "NOT_APPLICABLE"} />{claim.paymentIntentId ? <span>Ref: {claim.paymentIntentId}</span> : null}</div>
                                                    </div>
                                                    <div className="flex flex-wrap items-center gap-2">
                                                        <StatusBadge status={claim.status} />
                                                        {claim.status === "PENDING" && <><Button size="sm" className="bg-emerald-600 text-white hover:bg-emerald-700" onClick={() => handleClaimStatus(claim.id, "approve")}>Approve</Button><Button size="sm" variant="destructive" onClick={() => handleClaimStatus(claim.id, "reject")}>Reject</Button></>}
                                                    </div>
                                                </div>
                                            </div>)}
                                        </div>
                                    </div>
                                )}
                            </Card>;
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}
