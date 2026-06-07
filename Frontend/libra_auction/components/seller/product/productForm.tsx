"use client";
import { useState, useRef, useEffect } from "react";
import { Attribute } from "@/types/product/attribute";
import Image from "next/image";
import { Category } from "@/types/category";
import { fetchCategories } from "@/services/fetch_categories";
import { uploadImageToCloudinary } from "@/services/image_upload_to_cloudinary";
import { fetchImageUploadConfig } from "@/services/fetch_image_upload_config";
import { NewProduct } from "@/types/product/new-product";
import { createProduct } from "@/services/create_product";
import { fetchAttributeNames, fetchAttributeValues } from "@/services/fetch_standardized_attributes";
import { getErrorMessage } from "@/lib/app_error";

export default function ProductForm() {
  const [attributes, setAttributes] = useState<Attribute[]>([]);
  const [images, setImages] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // System attribute picker state
  const [showSystemPicker, setShowSystemPicker] = useState(false);
  const [attrNames, setAttrNames] = useState<string[]>([]);
  const [attrNameSearch, setAttrNameSearch] = useState("");
  const [selectedAttrName, setSelectedAttrName] = useState<string | null>(null);
  const [attrValues, setAttrValues] = useState<{ id: string; attributeName: string; attributeValue: string }[]>([]);
  const [attrValueSearch, setAttrValueSearch] = useState("");

  useEffect(() => {
    const loadCategories = async () => {
      const categories: Category[] = await fetchCategories();
      setCategories(categories);
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
    // Prevent duplicates
    const exists = attributes.some(a => a.isSystem && a.key === name && a.value === value);
    if (!exists) {
      setAttributes([...attributes, { key: name, value, isSystem: true }]);
    }
    // Reset picker
    setShowSystemPicker(false);
    setSelectedAttrName(null);
    setAttrNameSearch("");
    setAttrValueSearch("");
  };

  const removeAttribute = (index: number) => {
    setAttributes(attributes.filter((_, i) => i !== index));
  };

  const updateAttribute = (index: number, field: keyof Attribute, val: string) => {
    const newAttrs = [...attributes];
    newAttrs[index] = { ...newAttrs[index], [field]: val };
    setAttributes(newAttrs);
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const newFiles = Array.from(e.target.files);
      setImages((prev) => [...prev, ...newFiles]);

      const newPreviews = newFiles.map((file) => URL.createObjectURL(file));
      setPreviews((prev) => [...prev, ...newPreviews]);
    }
  };

  const removeImage = (index: number) => {
    setImages(images.filter((_, i) => i !== index));
    URL.revokeObjectURL(previews[index]);
    setPreviews(previews.filter((_, i) => i !== index));
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
      const uploadPromises = images.map(async (img) => {
        const imgUploadConfig = await fetchImageUploadConfig("products", img.name);
        return await uploadImageToCloudinary(img, imgUploadConfig);
      });

      const uploadedUrls = await Promise.all(uploadPromises);
      newProduct.imageUrls = uploadedUrls.filter((url): url is string => !!url);

      if (newProduct.imageUrls.length === 0 && images.length > 0) {
        setError("Unable to upload images. Please try again.");
        return;
      }

      await createProduct(newProduct);
      setSuccess("Success! The product was created successfully.");
      window.location.replace("/seller-dashboard/products");
    } catch (submitError) {
      setError(getErrorMessage(submitError, "Failed to create product."));
    }
  };

  // Filter attribute names by search
  const filteredAttrNames = attrNames.filter(name =>
    name.toLowerCase().includes(attrNameSearch.toLowerCase())
  );

  // Filter attribute values by search
  const filteredAttrValues = attrValues.filter(v =>
    v.attributeValue.toLowerCase().includes(attrValueSearch.toLowerCase())
  );

  return (
    <form
      onSubmit={handleSubmit}
      className="bg-white p-8 rounded-2xl border border-[#AFD3E2] space-y-8 shadow-sm"
    >
      <h2 className="text-2xl font-bold text-[#146C94]">Add New Product</h2>
      {error ? (
        <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</p>
      ) : null}
      {success ? (
        <p className="rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-700">{success}</p>
      ) : null}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-bold text-[#146C94]">Product name</label>
          <input
            required
            className="border border-[#AFD3E2] p-2.5 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#19A7CE] transition-all"
            name="name"
            placeholder="Example: iPhone 15 Pro Max"
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <label className="text-sm font-bold text-[#146C94]">Quantity</label>
          <input
            required
            type="number"
            min="1"
            className="border border-[#AFD3E2] p-2.5 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#19A7CE] transition-all"
            name="quantity"
          />
        </div>
      </div>
      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-bold text-[#146C94]">Category</label>

        <select
          name="categoryId"
          required
          className="border border-[#AFD3E2] p-2.5 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#19A7CE]"
        >
          <option value="">-- Select a category --</option>

          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.title}
            </option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1.5">
        <label className="text-sm font-bold text-[#146C94]">Description</label>
        <textarea
          className="border border-[#AFD3E2] p-2.5 rounded-xl h-28 focus:outline-none focus:ring-2 focus:ring-[#19A7CE] transition-all resize-none"
          name="description"
          placeholder="Enter a detailed product description..."
        />
      </div>

      <div className="space-y-3">
        <label className="text-sm font-bold text-[#146C94]">Product images</label>
        <div className="flex flex-wrap gap-3">
          {previews.map((src, index) => (
            <div key={index} className="relative w-24 h-24 group">
              <Image src={src} width={96} height={96} className="w-full h-full object-cover rounded-xl border border-[#AFD3E2]" alt="preview" />
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs opacity-0 group-hover:opacity-100 transition-opacity shadow-md"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
              </button>
            </div>
          ))}

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="w-24 h-24 border-2 border-dashed border-[#AFD3E2] rounded-xl flex flex-col items-center justify-center text-[#19A7CE] hover:bg-[#F6F1F1] transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 5v14M5 12h14" /></svg>
            <span className="text-[10px] font-bold mt-1">ADD IMAGE</span>
          </button>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleImageChange}
            multiple
            accept="image/*"
            className="hidden"
          />
        </div>
      </div>

      <div className="space-y-4 pt-4 border-t border-[#F6F1F1]">
        <div className="flex justify-between items-center">
          <h3 className="font-bold text-[#146C94]">Product attributes</h3>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setShowSystemPicker(true)}
              className="text-[10px] font-bold uppercase bg-[#19A7CE] text-white px-3 py-1.5 rounded-lg hover:bg-[#146C94] transition-all shadow-sm"
            >
              + System
            </button>
            <button
              type="button"
              onClick={addCustomAttribute}
              className="text-[10px] font-bold uppercase bg-[#AFD3E2] text-[#146C94] px-3 py-1.5 rounded-lg hover:bg-[#97c4d6] transition-all shadow-sm"
            >
              + Custom
            </button>
          </div>
        </div>

        {attributes.length === 0 && (
          <p className="text-xs text-gray-400 italic">No attributes have been added yet.</p>
        )}

        <div className="grid grid-cols-1 gap-3">
          {attributes.map((attr, index) => (
            <div key={index} className="flex gap-3 items-center animate-in fade-in slide-in-from-left-2 duration-300">
              <input
                placeholder={attr.isSystem ? "Attribute name (System)" : "Attribute name (Custom)"}
                className={`flex-1 border p-2.5 rounded-xl text-sm focus:outline-none focus:ring-1 focus:ring-[#19A7CE] transition-all ${attr.isSystem ? 'border-[#19A7CE] bg-blue-50/30 font-medium' : 'border-gray-200'
                  }`}
                value={attr.key}
                onChange={(e) => updateAttribute(index, 'key', e.target.value)}
                readOnly={attr.isSystem}
              />
              <input
                placeholder="Value"
                className={`flex-1 border p-2.5 rounded-xl text-sm focus:outline-none focus:ring-1 focus:ring-[#19A7CE] transition-all ${attr.isSystem ? 'border-[#19A7CE] bg-blue-50/30 font-medium' : 'border-gray-200'
                  }`}
                value={attr.value}
                onChange={(e) => updateAttribute(index, 'value', e.target.value)}
                readOnly={attr.isSystem}
              />
              <button
                type="button"
                onClick={() => removeAttribute(index)}
                className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-full transition-colors"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
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

      <button className="w-full bg-[#146C94] text-white py-4 rounded-xl font-bold hover:bg-[#19A7CE] transition-all active:scale-[0.98] shadow-lg shadow-blue-100">
        Create Product
      </button>
    </form>
  );
}
