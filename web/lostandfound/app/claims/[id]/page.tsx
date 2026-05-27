"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Loader2, Info, ShieldAlert, CheckCircle2 } from "lucide-react";
import { toast } from "sonner";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import { StripePaymentPanel } from "@/components/ui/stripe-payment-panel";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export default function ClaimDetailsPage() {
    const params = useParams();
    const router = useRouter();
    const claimId = params.id as string;
    
    const [loading, setLoading] = useState(true);
    const [claim, setClaim] = useState<any>(null);
    const [user, setUser] = useState<any>(null);
    const [isUpdating, setIsUpdating] = useState(false);

    const loadData = async () => {
        const token = localStorage.getItem("token");
        if (!token) return router.push("/login");

        try {
            const [userRes, claimRes] = await Promise.all([
                fetch(`${API_BASE}/users/me`, { headers: { Authorization: `Bearer ${token}` } }),
                fetch(`${API_BASE}/claims/${claimId}`, { headers: { Authorization: `Bearer ${token}` } }),
            ]);

            if (!userRes.ok || !claimRes.ok) {
                if (claimRes.status === 404) {
                    toast.error("Claim not found.");
                    router.push("/profile");
                    return;
                }
                throw new Error("Failed to load claim details.");
            }

            setUser(await userRes.json());
            setClaim(await claimRes.json());
        } catch (err: any) {
            toast.error(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (claimId) loadData();
    }, [claimId]);

    const handleUpdateStatus = async (action: "approve" | "reject") => {
        setIsUpdating(true);
        const token = localStorage.getItem("token");
        try {
            const res = await fetch(`${API_BASE}/claims/${claimId}/${action}`, {
                method: "POST",
                headers: { Authorization: `Bearer ${token}` }
            });
            const data = await res.json().catch(() => ({}));
            if (!res.ok) throw new Error(data.message || `Failed to ${action} claim.`);
            toast.success(`Claim ${action}d successfully.`);
            await loadData();
        } catch (err: any) {
            toast.error(err.message);
        } finally {
            setIsUpdating(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-[calc(100vh-4rem)]">
                <Loader2 className="h-10 w-10 animate-spin text-rose-800" />
            </div>
        );
    }

    if (!claim || !user) return null;

    
    const isClaimant = claim.claimantId === user.userId;
    const isPoster = claim.itemReporterId === user.userId;
    const isAdmin = user.role === "ADMIN";
    
    if (!isClaimant && !isPoster && !isAdmin) {
        return (
            <div className="flex justify-center items-center h-[calc(100vh-4rem)]">
                <p className="text-slate-500">You do not have permission to view this claim.</p>
            </div>
        );
    }

    
    const youNeedToPay = (isClaimant && claim.itemStatus === "FOUND") || (isPoster && claim.itemStatus === "LOST");
    const otherPartyNeedsToPay = (isClaimant && claim.itemStatus === "LOST") || (isPoster && claim.itemStatus === "FOUND");
    const isPaid = claim.paymentStatus === "PAID";
    const canPay = claim.status === "APPROVED" && !isPaid && youNeedToPay;

    return (
        <div className="min-h-[calc(100vh-4rem)] bg-slate-50 dark:bg-slate-950 py-8 px-4">
            <div className="container mx-auto max-w-3xl">
                <Button variant="ghost" onClick={() => router.back()} className="mb-6 -ml-4 text-slate-500 hover:text-slate-900 dark:hover:text-white">
                    <ArrowLeft className="mr-2 h-4 w-4" /> Back
                </Button>

                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
                    <div>
                        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">
                            Claim Details
                        </h1>
                        <p className="text-slate-500 mt-1">Claim #{claim.id} • Filed on {new Date(claim.createdAt).toLocaleDateString()}</p>
                    </div>
                    <div className="flex gap-2">
                        <StatusBadge status={claim.status} />
                        {isPaid && <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-400">Paid</Badge>}
                    </div>
                </div>

                <div className="space-y-6">
                    {}
                    <Card className="border-slate-200 shadow-sm dark:border-slate-800 dark:bg-slate-900/50">
                        <CardHeader className="bg-slate-100/50 dark:bg-slate-800/50 border-b">
                            <CardTitle className="text-lg">Information</CardTitle>
                        </CardHeader>
                        <CardContent className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <h3 className="text-sm font-semibold text-slate-500 mb-1">Item</h3>
                                <p className="font-medium text-slate-900 dark:text-white">{claim.itemTitle || `Item #${claim.itemId}`}</p>
                                <Badge variant="outline" className={`mt-2 text-xs ${claim.itemStatus === 'LOST' ? 'bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/30 dark:text-rose-400 dark:border-rose-900' : 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-400 dark:border-emerald-900'}`}>
                                    {claim.itemStatus}
                                </Badge>
                            </div>
                            <div>
                                <h3 className="text-sm font-semibold text-slate-500 mb-1">Claimant</h3>
                                <p className="font-medium text-slate-900 dark:text-white">{claim.claimantName || "Unknown"}</p>
                            </div>
                            <div className="md:col-span-2">
                                <h3 className="text-sm font-semibold text-slate-500 mb-1">Proof Description</h3>
                                <p className="text-slate-700 dark:text-slate-300 bg-slate-50 dark:bg-slate-900 p-3 rounded-md border text-sm">
                                    {claim.proofDescription || "No description provided."}
                                </p>
                            </div>
                            {claim.proofImagePath && (
                                <div className="md:col-span-2">
                                    <h3 className="text-sm font-semibold text-slate-500 mb-2">Proof Image</h3>
                                    <div className="rounded-lg overflow-hidden border inline-block max-w-sm">
                                        <a href={claim.proofImagePath} target="_blank" rel="noreferrer">
                                            <img src={claim.proofImagePath} alt="Proof" className="w-full h-auto object-cover hover:opacity-90 transition-opacity" />
                                        </a>
                                    </div>
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    {}
                    <Card className="border-slate-200 shadow-sm dark:border-slate-800">
                        <CardHeader>
                            <CardTitle className="text-lg">Status & Actions</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            
                            {}
                            {claim.status === "PENDING" && (
                                <div className="flex flex-col gap-4">
                                    <div className="flex items-start gap-3 text-amber-700 bg-amber-50 p-4 rounded-lg border border-amber-200 dark:bg-amber-950/20 dark:border-amber-900 dark:text-amber-400">
                                        <Info className="h-5 w-5 shrink-0 mt-0.5" />
                                        <div>
                                            <p className="font-medium">Waiting for Review</p>
                                            <p className="text-sm mt-1">This claim is pending review by the item poster.</p>
                                        </div>
                                    </div>
                                    
                                    {isPoster && !isAdmin && (
                                        <div className="flex gap-3 mt-2">
                                            <Button 
                                                onClick={() => handleUpdateStatus("approve")} 
                                                disabled={isUpdating}
                                                className="bg-emerald-600 hover:bg-emerald-700 text-white"
                                            >
                                                {isUpdating ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : "Approve Claim"}
                                            </Button>
                                            <Button 
                                                variant="destructive" 
                                                onClick={() => handleUpdateStatus("reject")}
                                                disabled={isUpdating}
                                            >
                                                {isUpdating ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : "Reject Claim"}
                                            </Button>
                                        </div>
                                    )}
                                </div>
                            )}

                            {}
                            {claim.status === "REJECTED" && (
                                <div className="flex items-start gap-3 text-rose-700 bg-rose-50 p-4 rounded-lg border border-rose-200 dark:bg-rose-950/20 dark:border-rose-900 dark:text-rose-400">
                                    <ShieldAlert className="h-5 w-5 shrink-0 mt-0.5" />
                                    <div>
                                        <p className="font-medium">Claim Rejected</p>
                                        <p className="text-sm mt-1">This claim was rejected by the item poster.</p>
                                    </div>
                                </div>
                            )}

                            {}
                            {claim.status === "APPROVED" && (
                                <div className="space-y-4">
                                    <div className="flex items-start gap-3 text-emerald-700 bg-emerald-50 p-4 rounded-lg border border-emerald-200 dark:bg-emerald-950/20 dark:border-emerald-900 dark:text-emerald-400">
                                        <CheckCircle2 className="h-5 w-5 shrink-0 mt-0.5" />
                                        <div>
                                            <p className="font-medium">Claim Approved</p>
                                            <p className="text-sm mt-1">The claim has been verified and approved.</p>
                                        </div>
                                    </div>

                                    {!isPaid && (
                                        <div className="mt-6 border-t pt-6 border-slate-100 dark:border-slate-800">
                                            {canPay ? (
                                                <div className="max-w-md mx-auto">
                                                    <StripePaymentPanel
                                                        claimId={claim.id}
                                                        amount={5000}
                                                        currency="php"
                                                        description={claim.itemStatus === "LOST" ? `Send reward payment to the finder (${claim.claimantName})` : `Pay recovery fee to the finder`}
                                                        onSuccess={() => loadData()}
                                                    />
                                                </div>
                                            ) : otherPartyNeedsToPay ? (
                                                <div className="text-slate-500 text-sm bg-slate-50 dark:bg-slate-900 p-4 rounded-lg border">
                                                    <p>Waiting for the other party to complete the payment via Stripe.</p>
                                                </div>
                                            ) : isAdmin ? (
                                                <p className="text-sm text-slate-500">Waiting for payment completion between parties.</p>
                                            ) : null}
                                        </div>
                                    )}

                                    {isPaid && (
                                        <div className="mt-4 flex items-start gap-3 text-emerald-700 bg-emerald-50 p-4 rounded-lg border border-emerald-200 dark:bg-emerald-950/20 dark:border-emerald-900 dark:text-emerald-400">
                                            <CheckCircle2 className="h-5 w-5 shrink-0 mt-0.5" />
                                            <div>
                                                <p className="font-medium">Payment Completed</p>
                                                <p className="text-sm mt-1">Stripe reference: {claim.paymentIntentId}</p>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}

                        </CardContent>
                    </Card>

                </div>
            </div>
        </div>
    );
}
