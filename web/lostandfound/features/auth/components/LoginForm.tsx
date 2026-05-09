"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { LogIn } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { loginUser } from "@/features/auth/api";

export function LoginForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const data = await loginUser({ username, password });
      localStorage.setItem("token", data.token);
      toast.success("Login successful! Redirecting...");
      setTimeout(() => router.push("/items"), 800);
    } catch (error: any) {
      toast.error(error.message || "Failed to log in.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-slate-50 font-sans dark:bg-slate-950 px-4 relative overflow-hidden">
      <div className="absolute inset-0 z-0 bg-gradient-to-br from-amber-100/60 via-transparent to-transparent dark:from-amber-900/40" />
      <Card className="w-full max-w-md shadow-xl border-slate-200 dark:border-slate-800 z-10 relative bg-white/90 dark:bg-slate-900/90 backdrop-blur-md">
        <form onSubmit={handleLogin} className="space-y-3">
          <CardHeader className="text-center pb-4">
            <div className="mx-auto mb-4 bg-amber-100 dark:bg-amber-900/50 p-3 rounded-full w-fit">
              <LogIn className="w-6 h-6 text-amber-600 dark:text-amber-400" />
            </div>
            <CardTitle className="text-2xl font-bold">Welcome Back</CardTitle>
            <CardDescription>Enter your credentials to access your account.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <div className="flex flex-col space-y-1.5">
              <Label htmlFor="username">Username</Label>
              <Input id="username" placeholder="Type your username" value={username}
                onChange={(e) => setUsername(e.target.value)} required />
            </div>
            <div className="flex flex-col space-y-1.5">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" placeholder="Type your password" value={password}
                onChange={(e) => setPassword(e.target.value)} required />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col gap-4 mt-6 pb-6">
            <Button type="submit" className="w-full bg-rose-900 hover:bg-rose-950 text-white" disabled={isLoading}>
              {isLoading ? "Logging in..." : "Sign in"}
            </Button>
            <div className="text-center text-sm text-slate-500 w-full mb-2">
              Don&apos;t have an account?{" "}
              <Link href="/register" className="text-amber-600 hover:underline hover:text-amber-700 font-medium">
                Sign up
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
