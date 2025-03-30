<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class notificationController extends Controller
{
    public function index()
    {
        $notification = Notification::all();
        return response()->json([ 'notifications' => $notification, 'status' => 200 ], 200);
    }


    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title' => 'required',
            'message' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'time' => 'required|date',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Error en la validación de datos',
                'errors' => $validator->errors(),
                'status' => 400
            ], 400);
        }

         // Subir la imagen si existe
        $imagePath = null;
            if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/appointments', 'public'); 
         }

         $notification = Notification::create([
            'title' => $request->title,
            'message' => $request->message,
            'image' => $imagePath, // Guarda la ruta en la base de datos
            'time' => $request->time,
        ]);

        return response()->json(['notification' => $notification, 'status' => 201], 201);
    }


    public function show($id)
    {
        $notification = Notification::find($id);

        if (!$notification) {
            return response()->json(['message' => 'Notificacion no encontrada', 'status' => 404], 404);
        }

        return response()->json(['notification' => $notification, 'status' => 200], 200);
    }


    public function update(Request $request, $id)
    {
        $notification = Notification::find($id);

        if (!$notification) {
            return response()->json(['notification' => 'Notificacion no encontrada', 'status' => 404], 404);
        }

        $validator = Validator::make($request->all(), [
            'titile' => 'required',
            'message' => 'required',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'time' => 'required|date',      
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Error en la validación de datos', 'errors' => $validator->errors(), 'status' => 400], 400);
        }

        if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/notifications', 'public');
            $notification->image = $imagePath;
        }

        $notification->fill($request->except('image'));
        $notification->save();

        return response()->json(['notification' => $notification, 'message' => 'Notificacion actualizada', 'status' => 200], 200);
    }


    public function updatePartial(Request $request, $id)
    {
        $notification = Notification::find($id);

        if (!$notification) {
            return response()->json(['message' => 'Notificacion no encontrada', 'status' => 404], 404);
        }

        $notification->fill($request->only([ 'name', 'message', 'image', 'tyme']));
        $notification->save();

        return response()->json(['notification' => $notification, 'message' => 'Notificacion actualizada parcialmente', 'status' => 200], 200);
    }


    public function destroy($id)
    {
        $notification = Notification::find($id);

        if (!$notification) {
            return response()->json(['message' => 'Notificacion no encontrada', 'status' => 404], 404);
        }

        $notification->delete();

        return response()->json(['message' => 'Notificacion eliminada', 'status' => 200], 200);
    }


}
