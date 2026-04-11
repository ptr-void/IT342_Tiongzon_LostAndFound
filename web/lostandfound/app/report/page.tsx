"use client";

import { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { Loader2, UploadCloud, X } from "lucide-react";
import { toast } from "sonner";
import dynamic from "next/dynamic";

const MapPicker = dynamic(() => import("@/components/MapPicker"), { ssr: false });

export default function ReportItemPage() {
    const router = useRouter();
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [status, setStatus] = useState("LOST");
    const [category, setCategory] = useState("ELECTRONICS");
    const [locationDesc, setLocationDesc] = useState("");
    const [lat, setLat] = useState<number | null>(null);
    const [lng, setLng] = useState<number | null>(null);

    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [isUploading, setIsUploading] = useState(false);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [successMsg, setSuccessMsg] = useState<string | null>(null);
    const [reporterId, setReporterId] = useState<number | null>(null);

    useEffect(() => {
        const fetchUser = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                router.push("/login");
                return;
            }

            try {
                const res = await fetch("http://localhost:8080/api/users/me", {
                    headers: {
                        "Authorization": `Bearer ${token}`
                    }
                });
                if (res.ok) {
                    const data = await res.json();
                    setReporterId(data.userId);
                } else if (res.status === 401) {
                    localStorage.removeItem("token");
                    router.push("/login");
                }
            } catch (err) {
                console.error("Failed to fetch user:", err);
            }
        };

        fetchUser();
    }, [router]);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) {
                toast.error("Image must be smaller than 5MB");
                return;
            }
            if (!file.type.startsWith('image/')) {
                toast.error("Please select a valid image file");
                return;
            }
            setImageFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleRemoveImage = () => {
        setImageFile(null);
        setImagePreview(null);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const uploadToCloudinary = async (file: File): Promise<string | null> => {
        const formData = new FormData();
        formData.append("file", file);
        formData.append("upload_preset", "lost_and_found_unsigned");
        formData.append("cloud_name", "defkzzqcs");

        try {
            const res = await fetch("https://api.cloudinary.com/v1_1/defkzzqcs/image/upload", {
                method: "POST",
                body: formData
            });
            const data = await res.json();
            if (res.ok) {
                return data.secure_url;
            } else {
                console.error("Cloudinary upload error:", data);
                toast.error("Failed to upload image to cloud.");
                return null;
            }
        } catch (err) {
            console.error("Network error during image upload:", err);
            toast.error("Network error during image upload.");
            return null;
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccessMsg(null);

        if (!reporterId) {
            setError("Please wait. Loading user identity...");
            return;
        }

        setIsSubmitting(true);
        const token = localStorage.getItem("token");

        let imagePath: string | null = null;
        if (imageFile) {
            setIsUploading(true);
            imagePath = await uploadToCloudinary(imageFile);
            setIsUploading(false);
            if (!imagePath) {
                setIsSubmitting(false);
                return;
            }
        }

        const payload = {
            title,
            description,
            status,
            category,
            locationDescription: locationDesc,
            locationLat: lat,
            locationLng: lng,
            reporterId,
            imagePath
        };

        try {
            const response = await fetch("http://localhost:8080/api/items", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const data = await response.json();
                setSuccessMsg(`Item reported successfully! ID: ${data.id}`);
                setTitle("");
                setDescription("");
                setLocationDesc("");
                setLat(null);
                setLng(null);
                handleRemoveImage();
                toast.success("Item reported successfully!");
                setTimeout(() => {
                    router.push("/items");
                }, 2000);
            } else {
                const errData = await response.json();
                setError(errData.message || "Failed to report item.");
            }
        } catch (err) {
            setError("Network error occurred.");
            console.error(err);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="container mx-auto py-6 px-4 max-w-2xl">
            <Card className="border-rose-100 shadow-md overflow-hidden">
                <CardHeader className="bg-rose-50/50 border-b items-center border-rose-100 mb-4">
                    <CardTitle className="text-2xl mt-5 font-bold text-rose-900 flex items-center gap-2">
                        Report an Item
                    </CardTitle>
                    <CardDescription className="text-rose-700/80">
                        Fill out the details to report a lost or found item.
                    </CardDescription>
                </CardHeader>
                <form onSubmit={handleSubmit}>
                    <CardContent className="space-y-6">
                        {error && (
                            <div className="bg-red-50 text-red-600 p-3 rounded-md text-sm border border-red-200">
                                {error}
                            </div>
                        )}
                        {successMsg && (
                            <div className="bg-emerald-50 text-emerald-600 p-3 rounded-md text-sm border border-emerald-200">
                                {successMsg}
                            </div>
                        )}

                        <div className="space-y-2">
                            <Label htmlFor="title" className="text-slate-700 font-semibold">Title</Label>
                            <Input
                                id="title"
                                placeholder="E.g. Blue Hydro Flask or Found iPhone 13"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                required
                                className="border-slate-200 focus-visible:ring-rose-500"
                            />
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <Label className="text-slate-700 font-semibold">Status</Label>
                                <div className="grid grid-cols-2 gap-2 h-10">
                                    <button
                                        type="button"
                                        onClick={() => setStatus("LOST")}
                                        className={`rounded-md border flex items-center justify-center font-medium transition-all ${status === "LOST"
                                            ? "bg-rose-100 border-rose-200 text-rose-800 shadow-sm"
                                            : "bg-white border-slate-200 text-slate-600 hover:bg-slate-50"
                                            }`}
                                    >
                                        I lost this
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => setStatus("FOUND")}
                                        className={`rounded-md border flex items-center justify-center font-medium transition-all ${status === "FOUND"
                                            ? "bg-emerald-100 border-emerald-200 text-emerald-800 shadow-sm"
                                            : "bg-white border-slate-200 text-slate-600 hover:bg-slate-50"
                                            }`}
                                    >
                                        I found this
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="category" className="text-slate-700 font-semibold">Category</Label>
                                <Select value={category} onValueChange={setCategory}>
                                    <SelectTrigger className="border-slate-200">
                                        <SelectValue placeholder="Select Category" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="ELECTRONICS">Electronics</SelectItem>
                                        <SelectItem value="CLOTHING">Clothing</SelectItem>
                                        <SelectItem value="ACCESSORIES">Accessories</SelectItem>
                                        <SelectItem value="DOCUMENTS">Documents/IDs</SelectItem>
                                        <SelectItem value="OTHER">Other</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="description" className="text-slate-700 font-semibold">Description</Label>
                            <Textarea
                                id="description"
                                placeholder="Provide detailed description (color, brand, unique marks...)"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                required
                                rows={4}
                                className="border-slate-200 focus-visible:ring-rose-500 resize-none"
                            />
                        </div>

                        <div className="space-y-3 pt-2">
                            <Label className="text-slate-700 font-semibold block">Photo (Optional)</Label>

                            {!imagePreview ? (
                                <div
                                    className="border-2 border-dashed border-slate-300 rounded-lg p-6 flex flex-col items-center justify-center bg-slate-50 hover:bg-slate-100 transition-colors cursor-pointer group"
                                    onClick={() => fileInputRef.current?.click()}
                                >
                                    <div className="h-12 w-12 bg-white rounded-full flex items-center justify-center shadow-sm mb-3 group-hover:scale-105 transition-transform">
                                        <UploadCloud className="h-6 w-6 text-slate-500 group-hover:text-rose-600 transition-colors" />
                                    </div>
                                    <p className="text-sm font-medium text-slate-700 mb-1">Click to upload an image</p>
                                    <p className="text-xs text-slate-500">PNG, JPG, JPEG up to 5MB</p>
                                </div>
                            ) : (
                                <div className="relative rounded-lg overflow-hidden border border-slate-200 shadow-sm max-w-sm mx-auto">
                                    <img
                                        src={imagePreview}
                                        alt="Preview"
                                        className="w-full h-48 object-cover"
                                    />
                                    <button
                                        type="button"
                                        onClick={handleRemoveImage}
                                        className="absolute top-2 right-2 p-1.5 bg-black/50 hover:bg-black/70 rounded-full text-white transition-colors"
                                        title="Remove image"
                                    >
                                        <X className="h-4 w-4" />
                                    </button>
                                </div>
                            )}

                            <input
                                type="file"
                                accept="image/*"
                                className="hidden"
                                ref={fileInputRef}
                                onChange={handleImageChange}
                            />
                        </div>

                        <div className="space-y-2 pt-2 border-t border-slate-100">
                            <Label htmlFor="locationDesc" className="text-slate-700 font-semibold">Location</Label>
                            <div className="flex gap-2 flex-col sm:flex-row">
                                <Input
                                    id="locationDesc"
                                    placeholder="Where was this lost/found?"
                                    value={locationDesc}
                                    onChange={(e) => setLocationDesc(e.target.value)}
                                    className="border-slate-200 focus-visible:ring-rose-500 flex-1"
                                />
                            </div>

                            <div className="mt-4 border-2 border-slate-200 dark:border-slate-700 rounded-xl overflow-hidden shadow-sm h-[300px]">
                                <MapPicker
                                    onLocationSelect={(lat: number, lng: number) => {
                                        setLat(lat);
                                        setLng(lng);
                                    }}
                                    initialLat={lat || undefined}
                                    initialLng={lng || undefined}
                                />
                            </div>
                            {(lat && lng) && (
                                <p className="text-xs text-slate-500 mt-1">
                                    Pin dropped at: {lat.toFixed(4)}, {lng.toFixed(4)}
                                </p>
                            )}
                        </div>
                    </CardContent>
                    <CardFooter className="bg-slate-50/50 border-t border-slate-100 rounded-b-xl pt-6">
                        <Button
                            type="submit"
                            className="w-full bg-rose-800 hover:bg-rose-900 text-white shadow-md relative overflow-hidden"
                            disabled={isSubmitting || isUploading || reporterId === null}
                        >
                            {(isSubmitting || isUploading) && (
                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            )}
                            {isUploading ? "Uploading Image..." : isSubmitting ? "Submitting..." : "Submit Report"}
                        </Button>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
}
