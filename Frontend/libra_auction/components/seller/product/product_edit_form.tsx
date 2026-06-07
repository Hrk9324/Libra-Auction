"use client";
import { useState, useRef, useEffect } from "react";
import { Product } from "@/types/product/product";
import { Attribute } from "@/types/product/attribute";
import Image from "next/image";
import { Category } from "@/types/category";
import { fetchCategories } from "@/services/fetch_categories";
import { updateProduct } from "@/services/update_product";
import { NewProduct } from "@/types/product/new-product";
import { fetchImageUploadConfig } from "@/services/fetch_image_upload_config";
import { uploadImageToCloudinary } from "@/services/image_upload_to_cloudinary";
import { fetchAttributeNames, fetchAttributeValues } from "@/services/fetch_standardized_attributes";
import { getErrorMessage } from "@/lib/app_error";

export default function ProductEditForm({ initialData }: { initialData: Product }) {
  const [attributes, setAttributes] = useState<Attribute[]>(initialData.attributes || []);
  const [existingImages, setExistingImages] = useState<string[]>(initialData.images || []);
  const [newImageFiles, setNewImageFiles] = useState<File[]>([]);
  const [newPreviews, setNewPreviews] = useState<string[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState(initialData.category_id || "");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // System attribute picker state
  const [showSystemPicker, setShowSystemPicker] = useState(false);
  const [attrNames, setAttrNames] = useState<string[]>([]);
  const [attrNameSearch, setAttrNameSearch] = useState("");
  const [selectedAttrName, setSelectedAttrName] = useState<string | null>(null);
  const [attrValues, setAttrValues] = useState<{ id: string; attributeName: string; attributeValue: string }[]>([]);
  const [attrValueSearch, setAttrValueSearch] = useState("");

  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const data: Category[] = await fetchCategories();
        setCategories(data);
      } catch (e) {
        console.error("Fetch categories error:", e);
      }
    };
    loadCategories();
  }, []);

  // Load attribute names when system picker opens
  useEffect(() => {
    if (showSystemPicker && attrNames.length === 0) {
      fetchAttributeNames().then(setAttrNames);
    }
  }, [showSystemPicker, attrNames.length]);

  // Load values when an attribute name is selected
  useEffect(() => {
    if (selectedAttrName) {
      fetchAttributeValues(selectedAttrName).then(setAttrValues);
      setAttrValueSearch("");
    }
  }, [selectedAttrName]);

  const addCustomAttribute = () => {
    setAttributes([...attributes, { key: "", value: "", isSystem: false }]);
  };

  const addSystemAttribute = (name: string, value: string) => {
    const exists = attributes.some(a => a.isSystem && a.key === name && a.value === value);
    if (!exists) {
      setAttributes([...attributes, { key: name, value, isSystem: true }]);
    }
    setShowSystemPicker(false);
    setSelectedAttrName(null);
    setAttrNameSearch("");
    setAttrValueSearch("");
  };

  const updateAttribute = <K extends keyof Attribute>(index: number, field: K, val: Attribute[K]) => {
    const newAttrs = [...attributes];
    newAttrs[index] = { ...newAttrs[index], [field]: val };
    setAttributes(newAttrs);
  };

  const removeAttribute = (index: number) => {
    setAttributes(attributes.filter((_, i) => i !== index));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setNewImageFiles((prev) => [...prev, ...files]);
      const previews = files.map((file) => URL.createObjectURL(file));
      setNewPreviews((prev) => [...prev, ...previews]);
    }
  };

  const removeExistingImage = (url: string) => {
    setExistingImages((prev) => prev.filter((img) => img !== url));
  };

  const removeNewImage = (index: number) => {
    setNewImageFiles((prev) => prev.filter((_, i) => i !== index));
    URL.revokeObjectURL(newPreviews[index]);
    setNewPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit: React.ComponentProps<"form">["onSubmit"] = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      const formData = new FormData(e.currentTarget);
      const newProduct: NewProduct = {
        name: formData.get("name") as string,
        quantity: Number(formData.get("quantity")),
        categoryId: formData.get("categoryId") as string,
        description: formData.get("description") as string,
        attributes: attributes,
        imageUrls: []
      }
      const uploadPromises = newImageFiles.map(async (img) => {
        const imgUploadConfig = await fetchImageUploadConfig("products", img.name);
        return await uploadImageToCloudinary(img, imgUploadConfig);
      });

      const uploadedUrls = await Promise.all(uploadPromises);
      newProduct.imageUrls = existingImages.filter((url): url is string => !!url).concat(uploadedUrls.filter((url): url is string => !!url));

      if (newProduct.imageUrls.length === 0 && newImageFiles.length + existingImages.length > 0) {
        setError("Unable to upload images. Please try again.");
        return;
      }

      await updateProduct(initialData.product_id, newProduct);
      setSuccess("Success! The product was updated successfully.");
      window.location.replace("/seller-dashboard/products/" + initialData.product_id);
    } catch (submitError) {
      setError(getErrorMessage(submitError, "Failed to update product."));
    }
  };

  const filteredAttrNames = attrNames.filter(name =>
    name.toLowerCase().includes(attrNameSearch.toLowerCase())
  );

  const filteredAttrValues = attrValues.filter(v =>
    v.attributeValue.toLowerCase().includes(attrValueSearch.toLowerCase())
  );

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-white p-8 rounded-2xl border border-[#AFD3E2] space-y-8 shadow-sm"
    >
      <header className="border-b border-[#F6F1F1] pb-4">
        <h2 className="text-2xl font-bold text-[#146C94]">Edit Product</h2>
        <p className="text-sm text-gray-400 mt-1">Update the product details and attributes.</p>
      </header>

      {error ? (
        <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</p>
      ) : null}
      {success ? (
        <p className="rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-700">{success}</p>
      ) : null}

      {/* Thông tin chính */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="flex flex-col gap-2">
          <label className="text-sm font-bold text-[#146C94]">Product name</label>
          <input
            name="name"
            defaultValue={initialData.product_name}
            required
            className="border border-[#AFD3E2] p-3 rounded-xl focus:ring-2 focus:ring-[#19A7CE] outline-none transition-all"
          />
        </div>
        <div className="flex flex-col gap-2">
          <label className="text-sm font-bold text-[#146C94]">Quantity</label>
          <input
            type="number"
            name="quantity"
            defaultValue={initialData.quantity}
            required
            className="border border-[#AFD3E2] p-3 rounded-xl focus:ring-2 focus:ring-[#19A7CE] outline-none transition-all"
          />
        </div>
      </div>
      <div className="flex flex-col gap-2">
        <label className="text-sm font-bold text-[#146C94]">Category</label>
        <select
          name="categoryId"
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          required
          className="border border-[#AFD3E2] p-3 rounded-xl focus:ring-2 focus:ring-[#19A7CE]"
        >
          <option value="">-- Select a category --</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.title}
            </option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-2">
        <label className="text-sm font-bold text-[#146C94]">Detailed description</label>
        <textarea
          name="description"
          defaultValue={initialData.description}
          className="border border-[#AFD3E2] p-3 rounded-xl h-32 resize-none outline-none focus:ring-2 focus:ring-[#19A7CE] transition-all"
        />
      </div>

      {/* Quản lý Ảnh */}
      <div className="space-y-4">
        <label className="text-sm font-bold text-[#146C94] block">Product images</label>
        <div className="flex flex-wrap gap-4">
          {existingImages.map((url, index) => (
            <div key={`old-${index}`} className="relative w-28 h-28 group animate-in fade-in zoom-in">
              <Image src={url} width={96} height={96} className="w-full h-full object-cover rounded-xl border-2 border-[#19A7CE]" alt="old" />
              <button
                type="button"
                onClick={() => removeExistingImage(url)}
                className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-[10px] shadow-lg hover:scale-110 transition-transform"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
              </button>
            </div>
          ))}

          {newPreviews.map((src, index) => (
            <div key={`new-${index}`} className="relative w-28 h-28 group animate-in fade-in slide-in-from-bottom-2">
              <Image src={src} width={96} height={96} className="w-full h-full object-cover rounded-xl border-2 border-dashed border-green-500" alt="new" />
              <button
                type="button"
                onClick={() => removeNewImage(index)}
                className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-[10px] shadow-lg"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
              </button>
              <span className="absolute bottom-1 left-1 bg-green-500 text-white text-[8px] font-bold px-1.5 py-0.5 rounded shadow-sm">NEW</span>
            </div>
          ))}

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="w-28 h-28 border-2 border-dashed border-[#AFD3E2] rounded-xl flex flex-col items-center justify-center text-[#19A7CE] hover:bg-[#F6F1F1] transition-all"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M12 5v14M5 12h14" /></svg>
            <span className="text-[10px] font-bold mt-1">ADD IMAGE</span>
          </button>
          <input type="file" ref={fileInputRef} onChange={handleFileChange} multiple className="hidden" accept="image/*" />
        </div>
      </div>

      {/* Quản lý Thuộc tính */}
      <div className="space-y-4 pt-6 border-t border-[#F6F1F1]">
        <div className="flex justify-between items-center">
          <h3 className="font-bold text-[#146C94]">Specifications</h3>
          <div className="flex gap-2">
            <button type="button" onClick={() => setShowSystemPicker(true)} className="text-[10px] font-black bg-[#19A7CE] text-white px-3 py-2 rounded-lg shadow-sm active:scale-95 transition-all">SYSTEM</button>
            <button type="button" onClick={addCustomAttribute} className="text-[10px] font-black bg-[#AFD3E2] text-[#146C94] px-3 py-2 rounded-lg shadow-sm active:scale-95 transition-all">CUSTOM</button>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-3">
          {attributes.map((attr, index) => (
            <div key={index} className="flex gap-3 items-center group">
              <input
                placeholder={attr.isSystem ? "Attribute name (System)" : "Attribute name (Custom)"}
                className={`flex-1 border p-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-[#19A7CE] transition-all ${attr.isSystem ? 'border-[#19A7CE] bg-blue-50/20' : 'border-gray-200'
                  }`}
                value={attr.key}
                onChange={(e) => updateAttribute(index, 'key', e.target.value)}
                readOnly={attr.isSystem}
              />
              <input
                placeholder="Value"
                className={`flex-1 border p-2.5 rounded-xl text-sm outline-none focus:ring-1 focus:ring-[#19A7CE] transition-all ${attr.isSystem ? 'border-[#19A7CE] bg-blue-50/20' : 'border-gray-200'
                  }`}
                value={attr.value}
                onChange={(e) => updateAttribute(index, 'value', e.target.value)}
                readOnly={attr.isSystem}
              />
              <button
                type="button"
                onClick={() => removeAttribute(index)}
                className="p-2 text-red-300 hover:text-red-500 hover:bg-red-50 rounded-full transition-all"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M18 6 6 18M6 6l12 12" /></svg>
              </button>
            </div>
          ))}
        </div>

        {/* System Attribute Picker Modal */}
        {showSystemPicker && (
          <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
            <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl space-y-4">
              <div className="flex justify-between items-center">
                <h3 className="font-bold text-[#146C94] text-lg">
                  {selectedAttrName ? `Select value for "${selectedAttrName}"` : "Select attribute name"}
                </h3>
                <button
                  type="button"
                  onClick={() => {
                    setShowSystemPicker(false);
                    setSelectedAttrName(null);
                    setAttrNameSearch("");
                    setAttrValueSearch("");
                  }}
                  className="p-1 hover:bg-gray-100 rounded-full"
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M18 6 6 18M6 6l12 12" /></svg>
                </button>
              </div>

              {!selectedAttrName ? (
                <>
                  <input
                    type="text"
                    placeholder="Search attribute name..."
                    className="w-full border border-[#AFD3E2] p-2.5 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#19A7CE]"
                    value={attrNameSearch}
                    onChange={(e) => setAttrNameSearch(e.target.value)}
                    autoFocus
                  />
                  <div className="max-h-64 overflow-y-auto space-y-1">
                    {filteredAttrNames.length === 0 && (
                      <p className="text-sm text-gray-400 italic p-2">No attributes found</p>
                    )}
                    {filteredAttrNames.map((name) => (
                      <button
                        key={name}
                        type="button"
                        onClick={() => setSelectedAttrName(name)}
                        className="w-full text-left p-2.5 rounded-lg hover:bg-[#F6F1F1] transition-colors text-sm font-medium"
                      >
                        {name}
                      </button>
                    ))}
                  </div>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedAttrName(null);
                      setAttrValueSearch("");
                    }}
                    className="text-sm text-[#19A7CE] hover:underline"
                  >
                    &larr; Back to attribute names
                  </button>
                  <input
                    type="text"
                    placeholder="Search value..."
                    className="w-full border border-[#AFD3E2] p-2.5 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#19A7CE]"
                    value={attrValueSearch}
                    onChange={(e) => setAttrValueSearch(e.target.value)}
                    autoFocus
                  />
                  <div className="max-h-64 overflow-y-auto space-y-1">
                    {filteredAttrValues.length === 0 && (
                      <p className="text-sm text-gray-400 italic p-2">No values found</p>
                    )}
                    {filteredAttrValues.map((v) => (
                      <button
                        key={v.id}
                        type="button"
                        onClick={() => addSystemAttribute(v.attributeName, v.attributeValue)}
                        className="w-full text-left p-2.5 rounded-lg hover:bg-[#F6F1F1] transition-colors text-sm"
                      >
                        {v.attributeValue}
                      </button>
                    ))}
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </div>

      <footer className="pt-6">
        <button className="w-full bg-[#146C94] text-white py-4 rounded-xl font-bold text-lg hover:bg-[#19A7CE] shadow-lg shadow-blue-100 transition-all active:scale-[0.99]">
          UPDATE PRODUCT
        </button>
      </footer>
    </form>
  );
}
