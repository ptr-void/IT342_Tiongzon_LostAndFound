"use client";

import { useState, useEffect, useRef } from "react";
import { Loader2, QrCode, CheckCircle2, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export function StripePaymentPanel({
  claimId,
  amount,
  currency,
  description,
  onSuccess,
}: {
  claimId: number;
  amount: number;
  currency: string;
  description: string;
  onSuccess: () => void;
}) {
  const [step, setStep] = useState<"idle" | "loading" | "qr" | "done">("idle");
  const [qrUrl, setQrUrl] = useState<string | null>(null);
  const [paymentIntentId, setPaymentIntentId] = useState<string | null>(null);
  const [pollCount, setPollCount] = useState(0);
  const [err, setErr] = useState<string | null>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  
  useEffect(() => {
    return () => { if (pollRef.current) clearInterval(pollRef.current); };
  }, []);

  const stopPolling = () => {
    if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null; }
  };

  const startPolling = (intentId: string) => {
    stopPolling();
    pollRef.current = setInterval(async () => {
      setPollCount((n) => n + 1);
      try {
        const token = localStorage.getItem("token");
        const res = await fetch(`${API_BASE}/payments/verify-session/${intentId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const data = await res.json();
        if (data.succeeded) {
          stopPolling();
          
          const markRes = await fetch(`${API_BASE}/claims/${claimId}/mark-paid`, {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ paymentIntentId: intentId }), 
          });
          if (markRes.ok) {
            setStep("done");
            onSuccess();
          } else {
            const errData = await markRes.json().catch(() => ({}));
            setErr(errData.message || "Payment succeeded but failed to record.");
          }
        }
      } catch {
        
      }
    }, 3000);
  };

  const generateQR = async () => {
    setStep("loading");
    setErr(null);
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`${API_BASE}/payments/create-checkout-session`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ amount, currency }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to create payment.");

      const sessionId: string = data.sessionId;
      const paymentUrl: string = data.url;
      setPaymentIntentId(sessionId); 

      
      const qrImageUrl = `https://quickchart.io/qr?size=280&margin=2&text=${encodeURIComponent(paymentUrl)}`;
      setQrUrl(qrImageUrl);
      setStep("qr");

      
      startPolling(sessionId);
    } catch (e: any) {
      setErr(e.message || "Failed to generate QR.");
      setStep("idle");
    }
  };

  const cancel = () => {
    stopPolling();
    setStep("idle");
    setQrUrl(null);
    setPaymentIntentId(null);
    setPollCount(0);
  };

  if (step === "done") {
    return (
      <div className="flex items-center gap-2 text-emerald-600 font-semibold text-sm py-2">
        <CheckCircle2 className="h-5 w-5" />
        Payment confirmed by Stripe! Ref: {paymentIntentId}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {}
      <div className="rounded-lg border border-slate-200 bg-slate-50 p-4 dark:border-slate-700 dark:bg-slate-800/50">
        <p className="text-sm font-semibold text-slate-700 dark:text-slate-300 mb-1">{description}</p>
        <p className="text-lg font-bold text-rose-800 dark:text-rose-400">
          {currency.toUpperCase()} {(amount / 100).toFixed(2)}
        </p>
        <p className="text-xs text-slate-400 mt-0.5">Powered by Stripe · Test mode</p>
      </div>

      {step === "idle" && (
        <>
          <Button onClick={generateQR} className="w-full bg-rose-900 text-white hover:bg-rose-950">
            <QrCode className="mr-2 h-4 w-4" />
            Generate Payment QR
          </Button>
          {err && <p className="text-sm text-rose-600">{err}</p>}
        </>
      )}

      {step === "loading" && (
        <div className="flex items-center justify-center py-6 gap-2 text-slate-500">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span className="text-sm">Generating QR code…</span>
        </div>
      )}

      {step === "qr" && qrUrl && (
        <div className="space-y-4">
          <div className="flex flex-col items-center gap-3">
            <div className="rounded-xl border-2 border-rose-100 bg-white p-3 shadow-sm">
              <img src={qrUrl} alt="Payment QR Code" className="h-56 w-56 object-contain" />
            </div>
            <div className="text-center space-y-1">
              <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                Scan with your phone to pay
              </p>
              <p className="text-xs text-slate-500">
                Test card: <code className="bg-slate-100 dark:bg-slate-700 px-1 rounded">4242 4242 4242 4242</code> · any future date · any CVC
              </p>
            </div>
          </div>

          {}
          <div className="flex items-center justify-center gap-2 rounded-md border border-emerald-200 bg-emerald-50 dark:border-emerald-900/40 dark:bg-emerald-950/20 py-2 px-3">
            <RefreshCw className="h-3.5 w-3.5 animate-spin text-emerald-600" />
            <p className="text-xs text-emerald-700 dark:text-emerald-400 font-medium">
              Waiting for payment confirmation… (checking every 3s)
            </p>
          </div>

          {err && <p className="text-sm text-rose-600">{err}</p>}

          <Button variant="outline" className="w-full border-slate-300 text-slate-600" onClick={cancel}>
            Cancel
          </Button>
        </div>
      )}
    </div>
  );
}
