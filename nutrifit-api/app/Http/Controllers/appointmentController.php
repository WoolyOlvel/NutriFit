<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\Appointment;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;


class appointmentController extends Controller
{
    public function index()
    {
        $appointments = Appointment::all();
        return response()->json([ 'appointments' => $appointments, 'status' => 200 ], 200);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'type' => 'required',
            'time' => 'required|date',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'status' => 'required',
            'status_type' => 'required'
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

         $appointment = Appointment::create([
            'name' => $request->name,
            'type' => $request->type,
            'time' => $request->time,
            'image' => $imagePath, // Guarda la ruta en la base de datos
            'status' => $request->status,
            'status_type' => $request->status_type,
        ]);

        return response()->json(['appointment' => $appointment, 'status' => 201], 201);
    }


    public function show($id)
    {
        $appointment = Appointment::find($id);

        if (!$appointment) {
            return response()->json(['message' => 'Cita no encontrada', 'status' => 404], 404);
        }

        return response()->json(['appointment' => $appointment, 'status' => 200], 200);
    }

    public function update(Request $request, $id)
    {
        $appointment = Appointment::find($id);

        if (!$appointment) {
            return response()->json(['message' => 'Cita no encontrada', 'status' => 404], 404);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'required',
            'type' => 'required',
            'time' => 'required|date',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:2048',
            'status' => 'required',
            'status_type' => 'required'
        ]);

        if ($validator->fails()) {
            return response()->json(['message' => 'Error en la validación de datos', 'errors' => $validator->errors(), 'status' => 400], 400);
        }

        if ($request->hasFile('image')) {
            $imagePath = $request->file('image')->store('images/appointments', 'public');
            $appointment->image = $imagePath;
        }

        $appointment->fill($request->except('image'));
        $appointment->save();

        return response()->json(['appointment' => $appointment, 'message' => 'Cita actualizada', 'status' => 200], 200);
    }


    public function updatePartial(Request $request, $id)
    {
        $appointment = Appointment::find($id);

        if (!$appointment) {
            return response()->json(['message' => 'Cita no encontrada', 'status' => 404], 404);
        }

        $appointment->fill($request->only([ 'name', 'type', 'time', 'image', 'status', 'status_type' ]));
        $appointment->save();

        return response()->json(['appointment' => $appointment, 'message' => 'Cita actualizada parcialmente', 'status' => 200], 200);
    }

    public function destroy($id)
    {
        $appointment = Appointment::find($id);

        if (!$appointment) {
            return response()->json(['message' => 'Cita no encontrada', 'status' => 404], 404);
        }

        $appointment->delete();

        return response()->json(['message' => 'Cita eliminada', 'status' => 200], 200);
    }



}
