package com.example.waviapp.adapters;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import java.io.IOException;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.ViewHolder> {

    private PdfRenderer renderer;

    public PdfAdapter(ParcelFileDescriptor pfd) {
        try {
            this.renderer = new PdfRenderer(pfd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (renderer == null) return;
        
        PdfRenderer.Page page = renderer.openPage(position);
        
        // Create bitmap with high quality (2x scale)
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        
        holder.ivPage.setImageBitmap(bitmap);
        page.close();
    }

    @Override
    public int getItemCount() {
        return renderer != null ? renderer.getPageCount() : 0;
    }

    public void close() {
        if (renderer != null) {
            renderer.close();
            renderer = null;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPage;
        ViewHolder(View itemView) {
            super(itemView);
            ivPage = itemView.findViewById(R.id.ivPdfPage);
        }
    }
}
